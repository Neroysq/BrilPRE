package PREStruct;

import Bril.Expression;
import Utils.ExpSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class WorkListEarliest {

    static boolean update(CFG cfg, CFGBlock block) {
        WorkListInfo cur = block.preInfo.available;
        ExpSet orginalOut = new ExpSet(cur.out);

        // calc in
        cur.in = block.preds.size() > 0 ? new ExpSet(cfg.allExp) : new ExpSet();
        for (CFGBlock from : block.preds) {
            cur.in.intersect(from.preInfo.available.out);
        }

        // calc out: out = (in U in_ant) - kill
        cur.out = new ExpSet(cur.in);
        cur.out.union(block.preInfo.anticipated.in);
        for (String s : block.preInfo.kill) {
            ArrayList<Expression> removeList = new ArrayList<>();
            for (Expression exp : cur.out.expList) {
                if (exp.varInExp(s)) {
                    removeList.add(exp);
                }
            }
            cur.out.expList.removeAll(removeList);
            for (Expression exp : removeList) {
                cur.out.set.remove(ExpSet.expToString(exp));
            }
        }

        return !orginalOut.set.equals(cur.out.set); //TODO: might be wrong
    }

    static void init(CFG cfg) {
        for (CFGBlock block : cfg.blocks) {
            block.preInfo.available.in = new ExpSet(cfg.allExp);
            block.preInfo.available.out = new ExpSet(cfg.allExp);
        }
        cfg.entry.preInfo.available.in = new ExpSet();
        cfg.entry.preInfo.available.out = new ExpSet();
    }
    public static void work(CFG cfg) {
        init(cfg);
        LinkedList<CFGBlock> queue = new LinkedList<>(cfg.blocks);
        HashSet<CFGBlock> inque = new HashSet<>(cfg.blocks);

        while (queue.size() > 0) {
            CFGBlock cur = queue.remove();
            inque.remove(cur);
            if (update(cfg, cur)) {
                for (CFGBlock next : cur.succs)
                    if (!inque.contains(next))
                        queue.add(next);
            }
        }

        for (CFGBlock block : cfg.blocks) {
            block.preInfo.earliest = ExpSet.minus(block.preInfo.anticipated.in, block.preInfo.available.in);
        }
    }
}

