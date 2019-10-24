package Bril;

import Utils.ExpSet;
import Utils.Util;

import java.util.ArrayList;
import java.util.HashMap;

public class Expression {
    public String opName;
    public ArrayList<String> args;
    public Type type;
    public Expression(String opName, ArrayList<String> args, Type type) {
        this.type = type;
        if (opName.equals("id")) {
            System.err.println("BUG: generating exp for id op!");
        }
        this.opName = opName;
        this.args = args;
        if (opName.matches("add|mul|eq|and|or")) {
            // add, mul, eq, and, or
            String l = args.get(0), r = args.get(1);
            if (l.compareTo(r) > 0) {
                this.args.set(0, r);
                this.args.set(1, l);
            }
        } else if (opName.matches("lt|gt|le|ge")) {
            // lt, gt, le, ge
            String l = args.get(0), r = args.get(1);
            if (l.compareTo(r) > 0) {
                this.args.set(0, r);
                this.args.set(1, l);
                this.opName = opsite.get(opName);
            }
        }
    }
    public Expression(String opName, ArrayList<String> args) {
        this.type = ExpSet.getType(opName);
        if (opName.equals("id")) {
            System.err.println("BUG: generating exp for id op!");
        }
        this.opName = opName;
        this.args = args;
        if (opName.matches("add|mul|eq|and|or")) {
            // add, mul, eq, and, or
            String l = args.get(0), r = args.get(1);
            if (l.compareTo(r) > 0) {
                this.args.set(0, r);
                this.args.set(1, l);
            }
        } else if (opName.matches("lt|gt|le|ge")) {
            // lt, gt, le, ge
            String l = args.get(0), r = args.get(1);
            if (l.compareTo(r) > 0) {
                this.args.set(0, r);
                this.args.set(1, l);
                this.opName = opsite.get(opName);
            }
        }
    }
    static HashMap<String, String> opsite = new HashMap<>();
    static {
        opsite.put("lt", "gt");
        opsite.put("gt", "lt");
        opsite.put("le", "ge");
        opsite.put("ge", "le");
    }

    public boolean varInExp(String var) {
        for (String arg : args) {
            if (arg.equals(var))
                return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Expression) {
            Expression exp = (Expression) o;
            if (type != exp.type) return false;
            return ExpSet.expToString(this).equals(ExpSet.expToString(exp));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return ExpSet.expToString(this).hashCode() * 4 + type.hashCode();
    }

}
