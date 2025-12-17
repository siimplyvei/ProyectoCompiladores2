package minic.mips;

import minic.ir.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import minic.semantic.*;

public class MIPSGenerator {

    private final List<IRInstr> ir;
    private final StringBuilder out = new StringBuilder();

    private final StackFrame frame = new StackFrame();
    private final Map<String, String> tempRegs = new HashMap<>();
    private int tempCount = 0;
    private final ScopedSymbolTable symtab;


    public MIPSGenerator(List<IRInstr> ir, ScopedSymbolTable symtab) {
        this.ir = ir;
        this.symtab = symtab;
    }

    private String getTempReg(String temp) {
        return tempRegs.computeIfAbsent(temp, k -> "$t" + (tempCount++ % 10));
    }

    private int paramIndex = 0;
    private String currentFunction = null;

    private final StringBuilder data = new StringBuilder();
    private final Map<String, Integer> globalSizes = new HashMap<>();

    public void generate(String filename) throws IOException {

        collectGlobals();

        emit(".data");
        for (Map.Entry<String, Integer> e : globalSizes.entrySet()) {
            emit(e.getKey() + ": .space " + e.getValue());
        }

        emit(".text");
        emit(".globl main");


        for (IRInstr instr : ir) {
            if (instr instanceof IRBinOp) {
                emitBinOp((IRBinOp) instr);
            } else if (instr instanceof IRAssign) {
                emitAssign((IRAssign) instr);
            } else if (instr instanceof IRReturn) {
                emitReturn((IRReturn) instr);
            } else if (instr instanceof IRLabel) {
                emit(((IRLabel) instr).name + ":");
            } else if (instr instanceof IRGoto) {
                emit("j " + ((IRGoto) instr).target.name);
            } else if (instr instanceof IRIfZ) {
                IRIfZ ifz = (IRIfZ) instr;

                String cond = loadOperand(ifz.condition);

                // normalizar condición: cond != 0
                emit("beq " + cond + ", $zero, " + ifz.target.name);
            } else if (instr instanceof IRFuncBegin) {
                IRFuncBegin fb = (IRFuncBegin) instr;
                currentFunction = fb.name;

                emit(fb.name + ":");
                emit("addi $sp, $sp, -32");
                emit("sw $ra, 28($sp)");
            } else if (instr instanceof IRFuncEnd) {

                if ("main".equals(currentFunction)) {
                    emit("li $v0, 10");
                    emit("syscall");
                } else {
                    emit("lw $ra, 28($sp)");
                    emit("addi $sp, $sp, 32");
                    emit("jr $ra");
                }

                currentFunction = null;
            } else if (instr instanceof IRParam) {
                IRParam p = (IRParam) instr;
                String reg = loadOperand(p.value);
                emit("move $a" + paramIndex + ", " + reg);
                paramIndex++;
            } else if (instr instanceof IRCall) {
                IRCall call = (IRCall) instr;
                emit("jal " + call.funcName);
                if (call.result != null) {
                    String reg = getTempReg(call.result.toString());
                    emit("move " + reg + ", $v0");
                }
                paramIndex = 0;
            }else if (instr instanceof IRAddrOf) {
                emitAddrOf((IRAddrOf) instr);
            } else if (instr instanceof IRLoad) {
                emitLoad((IRLoad) instr);
            } else if (instr instanceof IRStore) {
                emitStore((IRStore) instr);
            }
        }

        try (FileWriter fw = new FileWriter(filename)) {
            fw.write(out.toString());
        }
    }

    private void emitBinOp(IRBinOp op) {
        String left = loadOperand(op.left);
        String right = loadOperand(op.right);
        String dest = getTempReg(op.result.toString());

        switch (op.op) {
            case "+":
                emit("add " + dest + ", " + left + ", " + right);
                break;
            case "-":
                emit("sub " + dest + ", " + left + ", " + right);
                break;
            case "*":
                emit("mul " + dest + ", " + left + ", " + right);
                break;
            case "/":
                emit("div " + left + ", " + right);
                emit("mflo " + dest);
                break;
            case "<":
                emit("slt " + dest + ", " + left + ", " + right);
                break;
            case ">":
                emit("slt " + dest + ", " + right + ", " + left);
                break;
            case "==":
                emit("sub " + dest + ", " + left + ", " + right);
                emit("sltiu " + dest + ", " + dest + ", 1");
                break;
            case "!=":
                emit("sub " + dest + ", " + left + ", " + right);
                emit("sltu " + dest + ", $zero, " + dest);
                break;
            case "<=":
                emit("slt " + dest + ", " + right + ", " + left);
                emit("xori " + dest + ", " + dest + ", 1");
                break;
            case ">=":
                emit("slt " + dest + ", " + left + ", " + right);
                emit("xori " + dest + ", " + dest + ", 1");
                break;
        }
    }

    private void collectGlobals() {

        for (Symbol s : symtab.getSymbols()) {

            if (s instanceof FunctionSymbol) {
                continue;
            }

            if (s instanceof ArraySymbol) {
                ArraySymbol arr = (ArraySymbol) s;

                int total = 1;
                for (int d = 0; d < arr.getDimensionCount(); d++) {
                    total *= arr.getSize(d);
                }

                globalSizes.put(s.getName(), total * 4);

            } else {
                // variable simple
                globalSizes.put(s.getName(), 4);
            }
        }
    }


    private void emitAssign(IRAssign asg) {
        String src = loadOperand(asg.value);

        // global
        if (globalSizes.containsKey(asg.target)) {
            emit("la $t8, " + asg.target);
            emit("sw " + src + ", 0($t8)");
            return;
        }

        // local
        int offset = frame.allocate(asg.target);
        emit("sw " + src + ", " + offset + "($sp)");
    }

    private void emitReturn(IRReturn ret) {
        String value = loadOperand(ret.value);
        emit("move $v0, " + value);
    }

    private void emitAddrOf(IRAddrOf a) {
        String dst = getTempReg(a.dst.toString());

        if (globalSizes.containsKey(a.name)) {
            emit("la " + dst + ", " + a.name);
        } else {
            int offset = frame.allocate(a.name);
            emit("addi " + dst + ", $sp, " + offset);
        }
    }

    private void emitLoad(IRLoad l) {
        String dst = getTempReg(l.dst.toString());
        String addr = loadOperand(l.addr);
        emit("lw " + dst + ", 0(" + addr + ")");
    }

    private void emitStore(IRStore s) {
        String addr = loadOperand(s.addr);
        String val  = loadOperand(s.value);
        emit("sw " + val + ", 0(" + addr + ")");
    }

    private String loadOperand(String op) {

        // constante
        if (op.matches("\\d+")) {
            String r = getTempReg("const_" + op + "_" + tempCount);
            emit("li " + r + ", " + op);
            return r;
        }

        // temporal
        if (op.startsWith("t")) {
            return getTempReg(op);
        }

        // variable GLOBAL
        if (globalSizes.containsKey(op)) {
            emit("la $t8, " + op);
            emit("lw $t8, 0($t8)");
            return "$t8";
        }

        // variable LOCAL
        int offset = frame.allocate(op);
        emit("lw $t8, " + offset + "($sp)");
        return "$t8";
    }

    private void emit(String s) {
        out.append(s).append("\n");
    }
}
