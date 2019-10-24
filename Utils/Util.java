package Utils;

import Bril.*;
import PREStruct.VarNameGenerator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.spec.ECField;
import java.util.ArrayList;

public class Util {
    public static void swapStrings(String a, String b) {
        String c = a;
        a = b;
        b = c;
    }


    public static Program parseJson(File jsonFile) throws Exception {
        String jsonString = new String(Files.readAllBytes(Paths.get(jsonFile.getPath())));
        JSONObject input = new JSONObject(jsonString);
        //System.out.println(input.toString(4));
        JSONArray functions = input.getJSONArray("functions");
        JSONObject mainFunc = functions.getJSONObject(0);
        JSONArray instrsJson = mainFunc.getJSONArray("instrs");
        ArrayList<Instruction> instrs = new ArrayList<>();
        for (int i = 0; i < instrsJson.length(); ++i) {
            JSONObject insJson = instrsJson.getJSONObject(i);
            Instruction ins = null;
            if (insJson.has("label")) {
                Label lbl = new Label(insJson.getString("label"));
                ins = lbl;
            } else if (insJson.getString("op").matches("const")) {
                if (insJson.getString("type").equals("int")) {
                    ConstIntOperation cio = new ConstIntOperation(insJson.getString("op"), insJson.getString("dest"), Type.tInt, insJson.getInt("value"));
                    ins = cio;
                }
                else {
                    ConstBoolOperation cbo = new ConstBoolOperation(insJson.getString("op"), insJson.getString("dest"), Type.tBool, insJson.getBoolean("value"));
                    ins = cbo;
                }
            } else {
                JSONArray argsJson = insJson.getJSONArray("args");
                ArrayList<String> args = new ArrayList<>();
                for (int j = 0; j < argsJson.length(); ++j) {
                    args.add(argsJson.getString(j));
                }
                if (insJson.getString("op").matches("add|mul|sub|div|eq|lt|gt|le|ge|not|and|or|id")) {

                    ValueOperation vo = new ValueOperation(insJson.getString("op"), insJson.getString("dest"),
                            insJson.getString("type").equals("int") ? Type.tInt : Type.tBool, args);
                    ins = vo;
                } else if (insJson.getString("op").matches("jmp|br|ret|nop|print")) {

                    EffectOperation eo = new EffectOperation(insJson.getString("op"), args);
                    ins = eo;
                }
            }
            instrs.add(ins);
            //System.out.println(ins.display());
        }
        ArrayList<Function> funcs = new ArrayList<>();
        Function main = new Function("main", instrs);
        funcs.add(main);
        Program program = new Program(funcs);
        return program;
    }

    public static JSONObject instrToJSONObject(Instruction instr) {
        JSONObject rtn = new JSONObject();
        if (instr instanceof Label) {
            Label lb = (Label) instr;
            rtn.put("label", lb.labelName);
        } else if (instr instanceof EffectOperation) {
            EffectOperation eo = (EffectOperation) instr;
            rtn.put("op", eo.op);
            JSONArray args = new JSONArray();
            for (String arg : eo.args)
                args.put(arg);
            rtn.put("args", args);
        } else if (instr instanceof ConstOperation) {
            if (instr instanceof ConstIntOperation) {
                ConstIntOperation cio = (ConstIntOperation) instr;
                rtn.put("op", "const");
                rtn.put("dest", cio.dest);
                rtn.put("type", "int");
                rtn.put("value", cio.value);
            } else {
                ConstBoolOperation cio = (ConstBoolOperation) instr;
                rtn.put("op", "const");
                rtn.put("dest", cio.dest);
                rtn.put("type", "bool");
                rtn.put("value", cio.value);
            }
        } else if (instr instanceof ValueOperation) {
            ValueOperation vo = (ValueOperation) instr;
            rtn.put("op", vo.opName);
            rtn.put("dest", vo.destName);
            rtn.put("type", vo.type == Type.tInt ? "int" : "bool");
            JSONArray args = new JSONArray();
            for (String arg : vo.args)
                args.put(arg);
            rtn.put("args", args);
        }
        return rtn;
    }

    public static JSONObject programToJSON(Program program) {
        JSONObject progJ = new JSONObject();
        JSONObject mainJ = new JSONObject();

        JSONArray instrsJ = new JSONArray();
        for (Instruction instr : program.functions.get(0).instrs) {
            instrsJ.put(instrToJSONObject(instr));
        }
        mainJ.put("instrs", instrsJ);

        mainJ.put("name", "main");
        JSONArray functionsJ = new JSONArray();
        functionsJ.put(mainJ);
        progJ.put("functions", functionsJ);
        return progJ;
    }
}
