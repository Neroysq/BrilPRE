package PREStruct;

import Bril.Expression;
import Bril.Instruction;
import Bril.Program;
import Utils.ExpSet;

import java.util.HashSet;

public class VarNameGenerator {
    String prefix;
    int labelCounter = 0;
    public VarNameGenerator(HashSet<String> varSet) {
        prefix = "_";
        while (true) {
            boolean conflict = false;
            for (String s : varSet) {
                if (s.startsWith(prefix)) {
                    conflict = true;
                    prefix += "_";
                    while (s.startsWith(prefix)) {
                        prefix += "_";
                    }
                    break;
                }
            }
            if (!conflict) return;
        }
    }

    public String genVarName(Expression exp) {
        return prefix + ExpSet.expToVarName(exp);
    }

    public String genLabelName() {
        String rtn = prefix + labelCounter;
        ++labelCounter;
        return rtn;
    }
}
