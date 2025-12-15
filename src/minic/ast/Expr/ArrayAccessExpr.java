package minic.ast;

import java.util.List;

public class ArrayAccessExpr extends Expr {
    public final String arrayName;
    public final List<Expr> indices; // 1D o 2D

    public ArrayAccessExpr(String name, List<Expr> indices) {
        this.arrayName = name;
        this.indices = indices;
    }
}
