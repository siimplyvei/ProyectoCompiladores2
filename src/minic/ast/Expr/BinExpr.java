package minic.ast;

public class BinExpr extends Expr {

    public final String op;
    public final Expr left;
    public final Expr right;

    public BinExpr(String op, Expr left, Expr right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        return "(" + left + " " + op + " " + right + ")";
    }
}
