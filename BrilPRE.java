import Bril.*;
import PREStruct.*;
import Utils.ExpSet;
import Utils.Util;
import org.json.JSONObject;
import picocli.CommandLine;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import static PREStruct.CFGBlock.linkBlocks;

@CommandLine.Command(name = "BrilPRE", version = "BrilPRE 0.1.0", mixinStandardHelpOptions = true,
        description = "A partial redundancy elimination pass for Bril")
public class BrilPRE implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "The source code file in JSON.")
    File inputFile;

    @CommandLine.Parameters(index = "1", description = "The output code file in JSON.")
    File outputFile;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new BrilPRE()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {


        JSONObject ans = Util.programToJSON(PREPass(Util.parseJson(inputFile)));
        Program program = Util.parseJson(inputFile);


        //save bytecode into disk
        FileWriter out = new FileWriter(outputFile);
        out.write(ans.toString(4));
        out.close();

        return 0;
    }

    Program PREPass(Program program) {
        CFG cfg = generateCFG(program);
        System.err.println("finished cfg generation");
        //cfg.display();
        preprocess(cfg); // precalc usedInBlock, kill;
        System.err.println("finished preprocess");
        //pass1_anticipated(cfg);
        WorkListAnticipated.work(cfg);
        System.err.println("finished pass1");
        //cfg.displayPRE();
        //pass2_earliest(cfg);
        WorkListEarliest.work(cfg);
        System.err.println("finished pass2");
        //cfg.displayPRE();
        //pass3_postponable(cfg);
        WorkListPostponable.work(cfg);
        System.err.println("finished pass3");
        //pass4_used(cfg);
        WorkListUsed.work(cfg);
        System.err.println("finished pass4");
        //cfg.displayPRE();

        VarNameGenerator vng = new VarNameGenerator(cfg.varSet);

        transform(cfg, vng);
        //cfg.display();
        return cfgToProgram(cfg, vng);
    }

    Program cfgToProgram(CFG cfg, VarNameGenerator vng) {
        Program program = new Program();
        ArrayList<Instruction> instrs = new ArrayList<>();
        ArrayList<CFGBlock> sortedBlocks = new ArrayList<>();

        for (CFGBlock block : cfg.blocks) {
            if (block == cfg.exit || block == cfg.entry) continue;
            if (block.originalNo < 0) continue;
            sortedBlocks.add(block);
            for (int i = 0; i < block.succs.size(); ++i) {
                CFGBlock succ = block.succs.get(i);
                if (succ.originalNo < 0 && succ != cfg.exit) {
                    Instruction lastInstr = block.instrs.getLast();
                    if (lastInstr instanceof EffectOperation && ((EffectOperation) lastInstr).op.matches("jmp|br")) {
                        EffectOperation eo = (EffectOperation) lastInstr;
                        if (eo.op.equals("jmp")) {
                            block.instrs.removeLast();
                            for (Instruction instr : succ.instrs) {
                                block.instrs.add(instr);
                            }
                            block.instrs.add(eo);
                            linkBlocks(block, succ, succ.succs.get(0));
                        } else { //br
                            Label labelInstr = (Label) succ.succs.get(0).instrs.getLast();
                            String labelName = labelInstr.labelName;
                            String newLabelName = vng.genLabelName();
                            Label newLabel = new Label(newLabelName);
                            succ.instrs.addFirst(newLabel);
                            for (int bri = 1; bri <= 2; ++bri) {
                                if (labelName.equals(eo.args.get(bri))) {
                                    eo.args.set(bri, newLabelName);
                                    ArrayList<String> jmpArgs = new ArrayList<>();
                                    jmpArgs.add(labelName);
                                    Instruction newJmp = new EffectOperation("jmp", jmpArgs);
                                    succ.instrs.addLast(newJmp);
                                    break;
                                }
                            }
                            sortedBlocks.add(succ);
                        }
                    } else { // no jump
                        for (Instruction instr : succ.instrs) {
                            block.instrs.add(instr);
                        }
                        linkBlocks(block, succ, succ.succs.get(0));
                    }
                }
            }
        }

        // System.err.println("final block seq: ");
        for (CFGBlock block : sortedBlocks) {
            //block.display();
            instrs.addAll(block.instrs);
        }

        Function funcMain = new Function("main", instrs);
        program.functions.add(funcMain);
        return program;
    }

    void transform(CFG cfg, VarNameGenerator vng) {
        for (CFGBlock block : cfg.blocks) {
            ExpSet tempExps = ExpSet.intersect(block.preInfo.latest, block.preInfo.toBeUsed.out);

            if (block.instrs.size() > 0) {
                Instruction instr = block.instrs.getFirst();
                if (instr instanceof ValueOperation) {
                    ValueOperation vo = (ValueOperation) instr;
                    if (!vo.opName.equals("id")) {
                        Expression exp = vo.getExp();
                        if (!block.preInfo.latest.contains(exp) || block.preInfo.toBeUsed.out.contains(exp)) {
                            ArrayList<String> newArgs = new ArrayList<>();
                            newArgs.add(vng.genVarName(exp));
                            Instruction newInstr = new ValueOperation("id", vo.destName, vo.type, newArgs);
                            block.instrs.removeFirst();
                            block.instrs.addFirst(newInstr);
                        }
                    }
                }
            }

            for (Expression exp : tempExps.expList) {
                String destName = vng.genVarName(exp);
                Instruction newInstr = new ValueOperation(exp.opName, destName, exp.type, exp.args);
                block.instrs.addFirst(newInstr);
            }

        }

        // remove empty blocks
        for (CFGBlock block : cfg.blocks) {
            if (block.instrs.size() < 1 && block != cfg.entry && block != cfg.exit) {
                // must len(preds) == 1 and len(succs) == 1
                CFGBlock pred = block.preds.get(0), succ = block.succs.get(0);
                linkBlocks(pred, block, succ);
            }
        }
    }

    void preprocess(CFG cfg) {
        // precalc usedInBlock, kill
        // each block at most contain 1 instr
        for (CFGBlock cur : cfg.blocks) {
            if (cur.instrs.size() < 1) continue;
            Instruction instr = cur.instrs.getFirst();
            if (instr instanceof ConstOperation) {
                // doesn't affect used, do kill
                if (instr instanceof ConstBoolOperation) {
                    String destName = ((ConstBoolOperation) instr).dest;
                    cur.preInfo.kill.add(destName);
                    cfg.varSet.add(destName);
                }
                else if (instr instanceof ConstIntOperation) { // constintoperation
                    String destName = ((ConstIntOperation) instr).dest;
                    cur.preInfo.kill.add(((ConstIntOperation) instr).dest);
                    cfg.varSet.add(destName);
                }
            } else if (instr instanceof ValueOperation) {
                ValueOperation vo = (ValueOperation) instr;
                // uniform exp
                // vo.setExp(vo.getExp());

                cur.preInfo.kill.add(vo.destName);
                if (!vo.opName.equals("id"))
                    cur.preInfo.usedInBlock.add(vo.getExp());

                cfg.varSet.add(vo.destName);
                for (String s : vo.args) {
                    cfg.varSet.add(s);
                }
            } else {
                if (instr instanceof Label) {
                    cfg.varSet.add(((Label) instr).labelName);
                }
                // EffectOp doesn't affect anything
            }
        }
    }

    CFG generateCFG(Program program) {
        // assume only one function main
        Function funcMain = program.functions.get(0);

        CFG cfg = new CFG();
        HashMap<Integer, CFGBlock> indexToBlock = new HashMap<>(); // will not lookup entry
        HashMap<String, CFGBlock> labelToBlock = new HashMap<>();
        indexToBlock.put(funcMain.instrs.size(), cfg.exit);

        // create one block for each instruction (label included)
        for (int i = 0; i < funcMain.instrs.size(); ++i) {
            CFGBlock newBlock = new CFGBlock();
            newBlock.originalNo = i;
            indexToBlock.put(i, newBlock);
            Instruction ins = funcMain.instrs.get(i);
            // System.err.println(i);
            newBlock.instrs.add(ins);
            if (ins instanceof Label) {
                labelToBlock.put(((Label) ins).labelName, newBlock);
            }

            cfg.blocks.add(newBlock);
        }
        cfg.calcAllExp();
        //cfg.display();

        // link nodes
        linkBlocks(cfg.entry, indexToBlock.get(0));
        for (int i = 0; i < funcMain.instrs.size(); ++i) {
            Instruction ins = funcMain.instrs.get(i);
            CFGBlock thisBlock = indexToBlock.get(i);
            if (ins instanceof EffectOperation &&
                    ((EffectOperation) ins).op.matches("jmp|br|ret")) {
                EffectOperation eo = (EffectOperation) ins;
                if (eo.op.equals("br")) {
                    String l = eo.args.get(1), r = eo.args.get(2);
                    // System.err.println("br " + l + " " + r);
                    linkBlocks(thisBlock, labelToBlock.get(l));
                    linkBlocks(thisBlock, labelToBlock.get(r));
                } else if (eo.op.equals("jmp")){ // jmp
                    String l = eo.args.get(0);
                    linkBlocks(thisBlock, labelToBlock.get(l));
                } else { //ret
                    linkBlocks(thisBlock, indexToBlock.get(funcMain.instrs.size()));
                }
            } else {
                linkBlocks(thisBlock, indexToBlock.get(i + 1));
            }
        }


        // add empty blocks for edges (a->b) where b has multiple preds
        ArrayList<CFGBlock> newBlocks = new ArrayList<>();
        for (CFGBlock thisBlock : cfg.blocks) {
            if (thisBlock.preds.size() > 1) {
                for (int i = 0; i < thisBlock.preds.size(); ++i) {
                    CFGBlock pred = thisBlock.preds.get(i);
                    CFGBlock newBlock = new CFGBlock();
                    for (int j = 0; j < pred.succs.size(); ++j) {
                        if (pred.succs.get(j) == thisBlock) {
                            pred.succs.set(j, newBlock);
                            newBlock.preds.add(pred);
                            newBlock.succs.add(thisBlock);
                            thisBlock.preds.set(i, newBlock);
                            break;
                        }
                    }
                    newBlocks.add(newBlock);
                }
            }
        }
        cfg.blocks.addAll(newBlocks);

        return cfg;
    }
}
