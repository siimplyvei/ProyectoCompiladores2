package minic.ir;

public class IRBinOp extends IRInstr {

    public final IRTemp result;
    public final String left;
    public final String op;
    public final String right;

    public IRBinOp(IRTemp result, String left, String op, String right) {
        this.result = result;
        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public String toString() {
        return result + " = " + left + " " + op + " " + right;
    }
}
