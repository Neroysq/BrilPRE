package PREStruct;

import Bril.Expression;
import Utils.ExpSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class WorkListAnticipated {

    static boolean update(CFG cfg, CFGBlock block) {
        System.err.println("update " + block.originalNo);
        WorkListInfo cur = block.preInfo.anticipated;
        ExpSet orginalIn = new ExpSet(cur.in);

        // calc out
        cur.out = block.succs.size() > 0 ? new ExpSet(cfg.allExp) : new ExpSet();
        for (CFGBlock from : block.succs) {
            cur.out.intersect(from.preInfo.anticipated.in);
        }

        // calc in: in = (out - kill) u usedinblock
        cur.in = new ExpSet(cur.out);
        for (String s : block.preInfo.kill) {
            ArrayList<Expression> removeList = new ArrayList<>();
            for (Expression exp : cur.in.expList) {
                if (exp.varInExp(s)) {
                    removeList.add(exp);
                }
            }
            cur.in.expList.removeAll(removeList);
            for (Expression exp : removeList) {
                cur.in.set.remove(ExpSet.expToString(exp));
            }
        }
        cur.in.union(block.preInfo.usedInBlock);

        System.err.println("original");
        orginalIn.display();
        System.err.println("current");
        cur.in.display();
        return !orginalIn.set.equals(cur.in.set); //TODO: might be wrong
    }

    static void init(CFG cfg) {
        for (CFGBlock block : cfg.blocks) {
            block.preInfo.anticipated.in = new ExpSet(cfg.allExp);
            block.preInfo.anticipated.out = new ExpSet(cfg.allExp);
        }
        cfg.exit.preInfo.anticipated.in = new ExpSet();
        cfg.exit.preInfo.anticipated.out = new ExpSet();
    }
    public static void work(CFG cfg) {
        init(cfg);
        System.err.println("finished init");
        LinkedList<CFGBlock> queue = new LinkedList<>(cfg.blocks);
        HashSet<CFGBlock> inque = new HashSet<>(cfg.blocks);

        while (queue.size() > 0) {
            System.err.println("working");
            CFGBlock cur = queue.remove();
            inque.remove(cur);
            if (update(cfg, cur)) {
                for (CFGBlock next : cur.preds)
                    if (!inque.contains(next))
                        queue.add(next);
            }
        }
    }
}
