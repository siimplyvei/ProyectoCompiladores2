package minic.mips;

import minic.ir.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MIPSGenerator {

    private final List<IRInstr> ir;
    private final StringBuilder out = new StringBuilder();

    private final StackFrame frame = new StackFrame();
    private final Map<String, String> tempRegs = new HashMap<>();
    private int tempCount = 0;

    public MIPSGenerator(List<IRInstr> ir) {
        this.ir = ir;
    }

    private String getTempReg(String temp) {
        return tempRegs.computeIfAbsent(temp, k -> "$t" + (tempCount++));
    }

    private int paramIndex = 0;

    public void generate(String filename) throws IOException {

        emit(".text");
        emit(".globl main");
        emit("main:");

        // Prologue
        emit("addi $sp, $sp, -32");
        emit("sw $ra, 28($sp)");

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
                emit("beq " + cond + ", $zero, " + ifz.target.name);
            } else if (instr instanceof IRFuncBegin) {
                emit(((IRFuncBegin) instr).name + ":");
                emit("addi $sp, $sp, -32");
                emit("sw $ra, 28($sp)");
            } else if (instr instanceof IRFuncEnd) {
                emit("lw $ra, 28($sp)");
                emit("addi $sp, $sp, 32");
                emit("jr $ra");
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
            }
        }

        // Epilogue
        emit("lw $ra, 28($sp)");
        emit("addi $sp, $sp, 32");
        emit("jr $ra");

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

    private void emitAssign(IRAssign asg) {
        String src = loadOperand(asg.value);
        int offset = frame.allocate(asg.target);
        emit("sw " + src + ", " + offset + "($sp)");
    }

    private void emitReturn(IRReturn ret) {
        String value = loadOperand(ret.value);
        emit("move $v0, " + value);
    }

    private String loadOperand(String op) {
        // constante
        if (op.matches("\\d+")) {
            String reg = "$t9";
            emit("li " + reg + ", " + op);
            return reg;
        }

        // temporal
        if (op.startsWith("t")) {
            return getTempReg(op);
        }

        // variable
        int offset = frame.allocate(op);
        String reg = "$t8";
        emit("lw " + reg + ", " + offset + "($sp)");
        return reg;
    }

    private void emit(String s) {
        out.append(s).append("\n");
    }
}
