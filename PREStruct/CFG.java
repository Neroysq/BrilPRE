package PREStruct;

import Bril.Instruction;
import Bril.ValueOperation;
import Utils.ExpSet;
import jdk.jshell.EvalException;

import java.util.ArrayList;
import java.util.HashSet;

public class CFG {
    public ArrayList<CFGBlock> blocks;
    public CFGBlock entry;
    public CFGBlock exit;
    public ExpSet allExp;
    public HashSet<String> varSet;
    public CFG() {
        entry = new CFGBlock();
        exit = new CFGBlock();
        blocks = new ArrayList<>();
        blocks.add(entry);
        blocks.add(exit);
        allExp = new ExpSet();
        varSet = new HashSet<>();
    }

    public void calcAllExp() {
        // System.err.println("callAllExp");
        for (CFGBlock block : blocks) {
            if (block.instrs.size() > 0) {
                Instruction instr = block.instrs.getFirst();
                if (instr instanceof ValueOperation && !((ValueOperation) instr).opName.equals("id")) {
                    allExp.add(((ValueOperation) instr).getExp());
                }
            }
        }
        // allExp.display();
        // System.err.println("callAllExp end");
    }

    public void display() {
        System.err.println("------display cfg begin-------");
        for (CFGBlock block : blocks) {
            block.display();
        }
        System.err.println("------display cfg end-------");
    }

    public void displayPRE() {
        System.err.println("------display cfgpre begin-------");
        allExp.display();
        for (CFGBlock block : blocks) {
            block.displayPRE();
        }
        System.err.println("------display cfg end-------");
    }


}
