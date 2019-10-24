package PREStruct;

import Bril.Instruction;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class CFGBlock {
    public ArrayList<CFGBlock> preds;
    public ArrayList<CFGBlock> succs;
    public ArrayDeque<Instruction> instrs;
    public PREBlockInfo preInfo;
    public int originalNo = -1;
    public CFGBlock() {
        preds = new ArrayList<>();
        succs = new ArrayList<>();
        instrs = new ArrayDeque<>();
        preInfo = new PREBlockInfo();
    }

    public CFGBlock(ArrayList<Instruction> instrs) {
        this.instrs = new ArrayDeque<>(instrs);
        preds = new ArrayList<>();
        succs = new ArrayList<>();
        preInfo = new PREBlockInfo();
    }

    public CFGBlock(Instruction instr) {
        this.instrs = new ArrayDeque<>();
        this.instrs.add(instr);
        preds = new ArrayList<>();
        succs = new ArrayList<>();
        preInfo = new PREBlockInfo();
    }

    public void addInsToHead(Instruction ins) {
        instrs.addFirst(ins);
    }

    public static void linkBlocks(CFGBlock a, CFGBlock b) {
        System.err.println("linking 2 blocks");
        System.err.println(a);
        System.err.println(b);
        a.succs.add(b);
        b.preds.add(a);
    }

    public static void linkBlocks(CFGBlock a, CFGBlock b, CFGBlock c) {
        for (int i = 0; i < a.succs.size(); ++i) {
            if (a.succs.get(i) == b) {
                a.succs.set(i, c);
                break;
            }
        }
        for (int i = 0; i < c.preds.size(); ++i) {
            if (c.preds.get(i) == b) {
                c.preds.set(i, c);
                break;
            }
        }
    }

    public void display() {
        System.err.println("---------CFGBlock display begin--------");
        System.err.println("LineNo: " + originalNo);
        System.err.println("preds: ");
        for (CFGBlock block : preds) {
            System.err.print(block.originalNo + " ");
        }
        System.err.println();


        System.err.println("succs: ");
        for (CFGBlock block : succs) {
            System.err.print(block.originalNo + " ");
        }
        System.err.println();
        System.err.println("instrs: ");
        for (Instruction instr : instrs) {
            System.err.println(instr.display());
        }
        System.err.println("---------CFGBlock display end--------");
    }

    public void displayPRE() {
        System.err.println("---------CFGBlock display begin--------");
        System.err.println("LineNo: " + originalNo);
        System.err.println("preds: ");
        for (CFGBlock block : preds) {
            System.err.print(block.originalNo + " ");
        }
        System.err.println();


        System.err.println("succs: ");
        for (CFGBlock block : succs) {
            System.err.print(block.originalNo + " ");
        }
        System.err.println();
        System.err.println("instrs: ");
        for (Instruction instr : instrs) {
            System.err.println(instr.display());
        }
        preInfo.display();
        System.err.println("---------CFGBlock display end--------");
    }
}
