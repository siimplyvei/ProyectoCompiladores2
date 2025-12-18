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

    private final Map<String, String> stringLiterals = new HashMap<>();
    private int stringCount = 0;

    private String getStringLabel(String value) {
        return stringLiterals.computeIfAbsent(value, v -> ".str" + (stringCount++));
    }

    private int paramIndex = 0;
    private String currentFunction = null;
    private final StringBuilder data = new StringBuilder();
    private final Map<String, Integer> globalSizes = new HashMap<>();
    private final Map<String, Integer> paramOffsets = new HashMap<>();

    private String mapBuiltin(String name) {
        switch (name) {
            case "printInt":
            case "print_int":
                return "__print_int";

            case "printChar":
            case "print_char":
                return "__print_char";

            case "printString":
            case "print_string":
            case "print_str":
                return "__print_string";

            case "readInt":
            case "read_int":
                return "__read_int";

            case "readChar":
            case "read_char":
                return "__read_char";

            case "readString":
            case "read_string":
                return "__read_string";

            case "println":
                return "__print_newline";

            default:
                return name;
        }
    }

    private void collectStringLiterals() {
        for (IRInstr instr : ir) {
            if (instr instanceof IRParam) {
                IRParam p = (IRParam) instr;
                if (p.value.startsWith("\"")) {
                    getStringLabel(p.value); // solo registra
                }
            }
        }
    }

    public void generate(String filename) throws IOException {
        collectGlobals();
        collectStringLiterals();

        emit(".data");
        emit("__strbuf: .space 256");

        // globales
        for (Map.Entry<String, Integer> e : globalSizes.entrySet()) {
            emit(e.getKey() + ": .space " + e.getValue());
        }

        // strings
        for (Map.Entry<String, String> e : stringLiterals.entrySet()) {
            String raw = e.getKey(); // incluye las comillas
            String label = e.getValue();
            emit(label + ": .asciiz " + raw);
        }

        emit(".text");
        emit(".globl main");
        emit("j main");

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
                String cond;
                if (ifz.condition.matches("\\d+")) {
                    // Si es literal 0, usar directamente registro temporal
                    cond = getTempReg("tmp_ifz_" + tempCount++);
                    emit("li " + cond + ", " + ifz.condition);
                } else {
                    cond = loadOperand(ifz.condition); // cargar el valor real de variable
                }
                emit("beq " + cond + ", $zero, " + ifz.target.name);
            } else if (instr instanceof IRFuncBegin) {
                IRFuncBegin fb = (IRFuncBegin) instr;
                currentFunction = fb.name;
                emit(fb.name + ":");
                emit("addi $sp, $sp, -32");
                emit("sw $ra, 28($sp)");

                paramOffsets.clear();

                paramOffsets.put("x", 0);

                emit("sw $a0, 0($sp)");
                emit("sw $a1, 4($sp)");
                emit("sw $a2, 8($sp)");
                emit("sw $a3, 12($sp)");
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
                // string literal
                if (p.value.startsWith("\"")) {
                    String label = getStringLabel(p.value);
                    emit("la $a0, " + label);
                } else {
                    String reg = loadOperand(p.value);
                    emit("move $a0, " + reg);
                }
                paramIndex++;
            } else if (instr instanceof IRCall) {
                IRCall call = (IRCall) instr;
                String target = mapBuiltin(call.funcName);
                emit("jal " + target);
                // solo si hay valor de retorno
                if (call.result != null && !"fill".equals(call.funcName)) {
                    String reg = getTempReg(call.result.toString());
                    emit("move " + reg + ", $v0");
                }
                paramIndex = 0;
            } else if (instr instanceof IRAddrOf) {
                emitAddrOf((IRAddrOf) instr);
            } else if (instr instanceof IRLoad) {
                emitLoad((IRLoad) instr);
            } else if (instr instanceof IRStore) {
                emitStore((IRStore) instr);
            }
        }

        emitRuntime();

        try (FileWriter fw = new FileWriter(filename)) {
            fw.write(out.toString());
        }
    }

    private void emitRuntime() {
        emit("");
        emit("__print_int:");
        emit("li $v0, 1");
        emit("syscall");
        emit("jr $ra");

        emit("__print_char:");
        emit("li $v0, 11");
        emit("syscall");
        emit("jr $ra");

        emit("__print_string:");
        emit("li $v0, 4");
        emit("syscall");
        emit("jr $ra");

        emit("__read_int:");
        emit("li $v0, 5");
        emit("syscall");
        emit("jr $ra");

        emit("__read_char:");
        emit("li $v0, 12");
        emit("syscall");
        emit("jr $ra");

        emit("__read_string:");
        emit("# $a0 = buffer");
        emit("# $a1 = length");
        emit("li $v0, 8");
        emit("syscall");
        emit("jr $ra");

        emit("__print_newline:");
        emit("li $a0, 10"); // '\n'
        emit("li $v0, 11"); // print_char
        emit("syscall");
        emit("jr $ra");
    }

    private void emitBinOp(IRBinOp op) {
        String left = loadOperand(op.left);
        String right = loadOperand(op.right);
        String dest = getTempReg(op.result.toString());
        switch (op.op) {
            case "+": emit("add " + dest + ", " + left + ", " + right); break;
            case "-": emit("sub " + dest + ", " + left + ", " + right); break;
            case "*": emit("mul " + dest + ", " + left + ", " + right); break;
            case "/": emit("div " + left + ", " + right); emit("mflo " + dest); break;
            case "<": emit("slt " + dest + ", " + left + ", " + right); break;
            case ">": emit("slt " + dest + ", " + right + ", " + left); break;
            case "==": emit("sub " + dest + ", " + left + ", " + right); emit("sltiu " + dest + ", " + dest + ", 1"); break;
            case "!=": emit("sub " + dest + ", " + left + ", " + right); emit("sltu " + dest + ", $zero, " + dest); break;
            case "<=": emit("slt " + dest + ", " + right + ", " + left); emit("xori " + dest + ", " + dest + ", 1"); break;
            case ">=": emit("slt " + dest + ", " + left + ", " + right); emit("xori " + dest + ", " + dest + ", 1"); break;
            case "%": emit("div " + left + ", " + right); emit("mfhi " + dest); break;
        }
    }

    private void collectGlobals() {
        for (Symbol s : symtab.getSymbols()) {
            if (s instanceof FunctionSymbol) continue;
            if (s instanceof ArraySymbol) {
                ArraySymbol arr = (ArraySymbol) s;
                int total = 1;
                for (int d = 0; d < arr.getDimensionCount(); d++) total *= arr.getSize(d);
                globalSizes.put(s.getName(), total * 4);
            } else {
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

        // global
        if (globalSizes.containsKey(a.name)) {
            emit("la " + dst + ", " + a.name);
            return;
        }

        // parámetro
        if (paramOffsets.containsKey(a.name)) {
            emit("addi " + dst + ", $sp, " + paramOffsets.get(a.name));
            return;
        }

        // variable local REAL
        int offset = frame.allocate(a.name);
        emit("addi " + dst + ", $sp, " + offset);
    }

    private void emitLoad(IRLoad l) {
        String dst = getTempReg(l.dst.toString());
        String addrReg = loadOperand(l.addr); // ← dirección real
        emit("lw " + dst + ", 0(" + addrReg + ")");
    }

    private void emitStore(IRStore s) {
        String addrReg = loadOperand(s.addr);
        String valReg = loadOperand(s.value);
        emit("sw " + valReg + ", 0(" + addrReg + ")");
    }

    private String loadOperand(String op) {

        // constante
        if (op.matches("\\d+")) {
            String r = getTempReg("const_" + tempCount++);
            emit("li " + r + ", " + op);
            return r;
        }

        // temporal
        if (op.startsWith("t")) {
            return getTempReg(op);
        }

        // global
        if (globalSizes.containsKey(op)) {
            emit("la $t8, " + op);
            emit("lw $t8, 0($t8)");
            return "$t8";
        }

        // VARIABLE LOCAL REAL
        int offset = frame.allocate(op);
        emit("lw $t8, " + offset + "($sp)");
        return "$t8";
    }

    private void emit(String s) {
        out.append(s).append("\n");
    }
}