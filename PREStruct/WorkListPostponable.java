package PREStruct;

import Bril.Expression;
import Utils.ExpSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class WorkListPostponable {

    static boolean update(CFG cfg, CFGBlock block) {
        WorkListInfo cur = block.preInfo.postponable;
        ExpSet orginalOut = new ExpSet(cur.out);

        // calc in
        cur.in = block.preds.size() > 0 ? new ExpSet(cfg.allExp) : new ExpSet();
        for (CFGBlock from : block.preds) {
            cur.in.intersect(from.preInfo.postponable.out);
        }

        // calc out: out = (in U in_ant) - kill
        cur.out = new ExpSet(cur.in);
        cur.out.union(block.preInfo.earliest);
        cur.out.minus(block.preInfo.usedInBlock);

        return !orginalOut.set.equals(cur.out.set); //TODO: might be wrong
    }

    static void init(CFG cfg) {
        for (CFGBlock block : cfg.blocks) {
            block.preInfo.postponable.in = new ExpSet(cfg.allExp);
            block.preInfo.postponable.out = new ExpSet(cfg.allExp);
        }
        cfg.entry.preInfo.postponable.in = new ExpSet();
        cfg.entry.preInfo.postponable.out = new ExpSet();
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
        /*
        for (CFGBlock block : cfg.blocks) {
            block.preInfo.earliest = ExpSet.minus(block.preInfo.anticipated.in, block.preInfo.available.in);
        }*/
    }
}

