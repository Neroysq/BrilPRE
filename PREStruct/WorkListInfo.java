package PREStruct;

import Utils.ExpSet;

public class WorkListInfo {
    public ExpSet in;
    public ExpSet out;
    public WorkListInfo() {
        in = new ExpSet();
        out = new ExpSet();
    }

    public void display() {
        System.err.println("-----in-----");
        in.display();
        System.err.println("-----out-----");
        out.display();
    }
}
