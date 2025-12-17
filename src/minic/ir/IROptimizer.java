package minic.ir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

public class IROptimizer {

    private static boolean isTemp(String v) {
        return v != null && v.startsWith("t");
    }

    private static boolean isTempOrVar(String v) {
        return v != null && !v.matches("\\d+");
    }

    public static List<IRInstr> constantFold(List<IRInstr> ir) {

        List<IRInstr> out = new ArrayList<>();

        for (IRInstr instr : ir) {

            if (instr instanceof IRBinOp) {
                IRBinOp bin = (IRBinOp) instr;

                if (isConst(bin.left) && isConst(bin.right)) {

                    int l = Integer.parseInt(bin.left);
                    int r = Integer.parseInt(bin.right);
                    int res = eval(l, r, bin.op);

                    // reemplazamos: tX = 2 + 3
                    // por:         tX = 5
                    out.add(new IRAssign(bin.result.toString(), String.valueOf(res)));
                    continue;
                }
            }

            out.add(instr);
        }

        return out;
    }

    private static boolean isConst(String s) {
        return s.matches("-?\\d+");
    }

    private static int eval(int l, int r, String op) {
        switch (op) {
            case "+": return l + r;
            case "-": return l - r;
            case "*": return l * r;
            case "/": return l / r;
            case "<": return l < r ? 1 : 0;
            case ">": return l > r ? 1 : 0;
            case "==": return l == r ? 1 : 0;
            case "!=": return l != r ? 1 : 0;
            case "<=": return l <= r ? 1 : 0;
            case ">=": return l >= r ? 1 : 0;
        }
        throw new RuntimeException("Op no soportado: " + op);
    }

    public static List<IRInstr> deadCodeElimination(List<IRInstr> ir) {

        Set<String> live = new HashSet<>();
        List<IRInstr> result = new ArrayList<>();

        ListIterator<IRInstr> it = ir.listIterator(ir.size());

        while (it.hasPrevious()) {
            IRInstr instr = it.previous();

            if (instr instanceof IRBinOp bin) {
                String dst = bin.result.toString();

                if (!live.contains(dst)) {
                    continue; // muerto
                }

                live.remove(dst);
                live.add(bin.left);
                live.add(bin.right);
                result.add(instr);

            } else if (instr instanceof IRLoad load) {
                String dst = load.dst.toString();

                if (!live.contains(dst)) {
                    continue;
                }

                live.remove(dst);
                live.add(load.addr);
                result.add(instr);

            } else if (instr instanceof IRCall call) {

                // IO tiene efectos secundarios
                if (call.funcName.startsWith("__")) {
                    if (call.result != null)
                        live.add(call.result.toString());
                    result.add(instr);
                    continue;
                }

                if (call.result != null && !live.contains(call.result.toString())) {
                    continue;
                }

                if (call.result != null)
                    live.remove(call.result.toString());

                result.add(instr);

            } else if (instr instanceof IRReturn ret) {
                live.add(ret.value);
                result.add(instr);

            } else if (instr instanceof IRIfZ ifz) {
                live.add(ifz.condition);
                result.add(instr);

            } else if (instr instanceof IRStore store) {
                live.add(store.addr);
                live.add(store.value);
                result.add(instr);

            } else {
                // labels, goto, func begin/end
                result.add(instr);
            }
        }

        Collections.reverse(result);
        return result;
    }

    public static List<IRInstr> removeConstantConditions(List<IRInstr> ir) {
        List<IRInstr> out = new ArrayList<>();

        for (int i = 0; i < ir.size(); i++) {
            IRInstr instr = ir.get(i);

            if (instr instanceof IRIfZ ifz) {
                if (ifz.condition.matches("\\d+")) {
                    int val = Integer.parseInt(ifz.condition);
                    if (val == 0) {
                        out.add(instr); // salto directo
                        continue;
                    }
                }
            }
            out.add(instr);
        }
        return out;
    }

    public static List<IRInstr> copyPropagation(List<IRInstr> ir) {
        Map<String, String> copies = new HashMap<>();
        List<IRInstr> result = new ArrayList<>();

        for (IRInstr instr : ir) {

            instr.replaceUses(copies);

            if (instr instanceof IRAssign) {
                IRAssign a = (IRAssign) instr;

                if (isTemp(a.target) && isTempOrVar(a.value)) {
                    copies.put(a.target, a.value);
                } else {
                    copies.remove(a.target);
                }
            }

            // barreras
            if (instr instanceof IRStore || instr instanceof IRCall) {
                copies.clear();
            }

            result.add(instr);
        }

        return result;
    }
}
