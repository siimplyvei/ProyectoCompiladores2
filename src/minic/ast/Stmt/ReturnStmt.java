package minic.ast;

public class ReturnStmt extends ASTNode {
    public final Expr expr;

    public ReturnStmt(Expr expr) {
        this.expr = expr;
    }

    @Override
    public String toString() {
        return "return " + expr;
    }
}
