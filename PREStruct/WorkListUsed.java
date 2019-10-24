package PREStruct;

import Bril.Expression;
import Utils.ExpSet;

import java.util.*;

public class WorkListUsed {

    static boolean update(CFG cfg, CFGBlock block) {
        WorkListInfo cur = block.preInfo.toBeUsed;
        ExpSet orginalIn = new ExpSet(cur.in);

        // calc out
        cur.out = new ExpSet();
        for (CFGBlock from : block.succs) {
            cur.out.union(from.preInfo.toBeUsed.in);
        }

        // calc in: in = (out - kill) u usedinblock
        cur.in = new ExpSet(cur.out);
        cur.in.union(block.preInfo.usedInBlock);
        cur.in.minus(block.preInfo.latest);

        return !orginalIn.set.equals(cur.in.set); //TODO: might be wrong
    }

    static void init(CFG cfg) {
        for (CFGBlock block : cfg.blocks) {
            block.preInfo.toBeUsed.in = new ExpSet();
            block.preInfo.toBeUsed.out = new ExpSet();
        }
    }
    public static void work(CFG cfg) {
        HashMap<CFGBlock, ExpSet> earliestUinPost = new HashMap<>();
        for (CFGBlock block : cfg.blocks) {
            earliestUinPost.put(block, ExpSet.union(block.preInfo.earliest, block.preInfo.postponable.in));
        }
        for (CFGBlock block : cfg.blocks) {
            ExpSet succEarliestUinPost = block.succs.size() > 0 ? new ExpSet(cfg.allExp) : new ExpSet();
            for (CFGBlock succ : block.succs) {
                succEarliestUinPost.intersect(earliestUinPost.get(succ));
            }

            block.preInfo.latest =
                    ExpSet.intersect(earliestUinPost.get(block),
                            ExpSet.union(block.preInfo.usedInBlock, ExpSet.minus(cfg.allExp, succEarliestUinPost))
                    );
        }

        init(cfg);
        LinkedList<CFGBlock> queue = new LinkedList<>(cfg.blocks);
        HashSet<CFGBlock> inque = new HashSet<>(cfg.blocks);

        while (queue.size() > 0) {
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
