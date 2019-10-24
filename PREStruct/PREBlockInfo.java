package PREStruct;

import Utils.ExpSet;

import java.util.HashSet;

public class PREBlockInfo {
    public WorkListInfo anticipated;
    public WorkListInfo available;
    public ExpSet earliest;
    public WorkListInfo postponable;
    public ExpSet latest;
    public WorkListInfo toBeUsed;
    public ExpSet usedInBlock;
    public HashSet<String> kill;

    public PREBlockInfo() {
        anticipated = new WorkListInfo();
        available = new WorkListInfo();
        earliest = new ExpSet();
        postponable = new WorkListInfo();
        latest = new ExpSet();
        toBeUsed = new WorkListInfo();
        usedInBlock = new ExpSet();
        kill = new HashSet<>();
    }

    public void display() {
        System.err.println("anticipated: ");
        anticipated.display();
        System.err.println("available: ");
        available.display();
        System.err.println("earliest: ");
        earliest.display();
        System.err.println("postponable: ");
        postponable.display();
        System.err.println("latest: ");
        latest.display();
        System.err.println("toBeUsed: ");
        toBeUsed.display();
        System.err.println("usedInBlock: ");
        usedInBlock.display();
        System.err.println("kill: ");
        for (String s : kill)
            System.err.print(s + " ");
        System.err.println("");
    }
}
