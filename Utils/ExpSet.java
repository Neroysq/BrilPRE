package Utils;

import Bril.EffectOperation;
import Bril.Expression;
import Bril.Instruction;
import Bril.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ExpSet {
    public HashSet<String> set;
    public HashSet<Expression> expList;
    public ExpSet() {
        set = new HashSet<>();
        expList = new HashSet<>();
    }
    public ExpSet(ExpSet expSet) {
        set = new HashSet<>(expSet.set);
        expList = new HashSet<>(expSet.expList);
    }
    public ExpSet(HashSet<String> set) {
        this.set = new HashSet<>(set);
        this.expList =  new HashSet<>();
        for (String s : set) {
            expList.add(stringToExp(s));
        }
    }

    public boolean contains(Expression exp) {
        return set.contains(expToString(exp));
    }

    public static String expToString(Expression exp) {
        // assuming of not op has 1 arg while others have 2 args
        // assume the expression is normalized
        if (exp.opName.equals("not")) {
            return exp.opName + " " + exp.args.get(0);
        } else {
            // add, mul, eq, and, or
            // lt, gt, le, ge
            // sub, div
            return exp.args.get(0) + " " + exp.opName + " " + exp.args.get(1);
        }
    }

    public static String expToVarName(Expression exp) {
        // assuming of not op has 1 arg while others have 2 args
        // assume the expression is normalized
        if (exp.opName.equals("not")) {
            return exp.opName + "_" + exp.args.get(0);
        } else {
            // add, mul, eq, and, or
            // lt, gt, le, ge
            // sub, div
            return exp.args.get(0) + "_" + exp.opName + "_" + exp.args.get(1);
        }
    }

    public static Expression stringToExp(String s) {
        String[] parseS = s.split(" ");
        ArrayList<String> args = new ArrayList<>();
        if (parseS[0].equals("not")) {
            args.add(parseS[1]);
            return new Expression(parseS[0], args);
        } else {
            args.add(parseS[0]);
            args.add(parseS[2]);
            return new Expression(parseS[1], args);
        }
    }

    public void add(Expression exp) {
        expList.add(exp);
        set.add(expToString(exp));
    }

    public static ExpSet union(ExpSet a, ExpSet b) {
        HashSet<String> rtn = new HashSet<>(a.set);
        rtn.addAll(b.set);
        return new ExpSet(rtn);
    }
    public void union(ExpSet a) {
        set.addAll(a.set);
        expList.addAll(a.expList);
    }

    public static ExpSet intersect(ExpSet a, ExpSet b) {
        HashSet<String> rtn = new HashSet<>(a.set);
        rtn.retainAll(b.set);
        return new ExpSet(rtn);
    }
    public void intersect(ExpSet a) {
        set.retainAll(a.set);
        expList.retainAll(a.expList);
    }

    public static ExpSet minus(ExpSet a, ExpSet b) {
        HashSet<String> rtn = new HashSet<>(a.set);
        rtn.removeAll(b.set);
        return new ExpSet(rtn);
    }
    public void minus(ExpSet a) {
        set.removeAll(a.set);
        expList.removeAll(a.expList);
    }

    public static Type getType(String opName) {
        if (opName.matches("add|mul|sub|div")) {
            return Type.tInt;
        } else {
            return Type.tBool;
        }
    }

    public void display() {
        for (String s : set) {
            System.err.println(s);
        }
        System.err.println("--");
        for (Expression s : expList) {
            System.err.println(expToString(s));
        }
    }
}
