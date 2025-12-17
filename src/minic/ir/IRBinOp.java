package minic.ir;

import java.util.*;

public class IRBinOp extends IRInstr {

    public final IRTemp result;
    public String left;
    public final String op;
    public String right;

    public IRBinOp(IRTemp result, String left, String op, String right) {
        this.result = result;
        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public void replaceUses(Map<String, String> copies) {
        left = repl(left, copies);
        right = repl(right, copies);
    }

    @Override
    public String toString() {
        return result + " = " + left + " " + op + " " + right;
    }
}
