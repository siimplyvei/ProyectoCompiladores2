package minic.ast;

public class AssignStmt extends ASTNode {
    public final String var;
    public final Expr expr;

    public AssignStmt(String var, Expr expr) {
        this.var = var;
        this.expr = expr;
    }

    @Override
    public String toString() {
        return var + " = " + expr;
    }
}
