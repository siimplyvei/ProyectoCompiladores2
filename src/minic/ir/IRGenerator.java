package minic.ir;

import minic.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;

public class IRGenerator extends MiniCBaseVisitor<String> {

    private final List<IRInstr> instructions = new ArrayList<>();

    public List<IRInstr> getInstructions() {
        return instructions;
    }

    // ---------- EXPRESSIONS ----------

    @Override
    public String visitPrimary(MiniCParser.PrimaryContext ctx) {
        if (ctx.IntegerConst() != null) {
            return ctx.IntegerConst().getText();
        }

        if (ctx.lvalue() != null) {
            return ctx.lvalue().Identifier().getText();
        }

        if (ctx.expr() != null) {
            return visit(ctx.expr());
        }

        return null;
    }

    @Override
    public String visitMultiplicativeExpr(MiniCParser.MultiplicativeExprContext ctx) {

        String result = visit(ctx.unaryExpr(0));

        for (int i = 1; i < ctx.unaryExpr().size(); i++) {
            String right = visit(ctx.unaryExpr(i));
            String op = ctx.getChild(2 * i - 1).getText();

            IRTemp temp = new IRTemp();
            instructions.add(new IRBinOp(temp, result, op, right));
            result = temp.toString();
        }

        return result;
    }

    @Override
    public String visitAdditiveExpr(MiniCParser.AdditiveExprContext ctx) {

        String result = visit(ctx.multiplicativeExpr(0));

        for (int i = 1; i < ctx.multiplicativeExpr().size(); i++) {
            String right = visit(ctx.multiplicativeExpr(i));
            String op = ctx.getChild(2 * i - 1).getText();

            IRTemp temp = new IRTemp();
            instructions.add(new IRBinOp(temp, result, op, right));
            result = temp.toString();
        }

        return result;
    }

    // ---------- STATEMENTS ----------

    @Override
    public String visitAssignStmt(MiniCParser.AssignStmtContext ctx) {
        String var = ctx.lvalue().Identifier().getText();
        String value = visit(ctx.expr());
        instructions.add(new IRAssign(var, value));
        return null;
    }

    @Override
    public String visitReturnStmt(MiniCParser.ReturnStmtContext ctx) {
        String value = visit(ctx.expr());
        instructions.add(new IRReturn(value));
        return null;
    }

    @Override
    public String visitRelationalExpr(MiniCParser.RelationalExprContext ctx) {

        String result = visit(ctx.additiveExpr(0));

        for (int i = 1; i < ctx.additiveExpr().size(); i++) {
            String right = visit(ctx.additiveExpr(i));
            String op = ctx.getChild(2 * i - 1).getText();

            IRTemp temp = new IRTemp();
            instructions.add(new IRBinOp(temp, result, op, right));
            result = temp.toString();
        }

        return result;
    }

    @Override
    public String visitIfStmt(MiniCParser.IfStmtContext ctx) {

        String cond = visit(ctx.expr());

        IRLabel elseLabel = new IRLabel();
        IRLabel endLabel = new IRLabel();

        instructions.add(new IRIfZ(cond, elseLabel));
        visit(ctx.statement(0));
        instructions.add(new IRGoto(endLabel));

        instructions.add(elseLabel);

        if (ctx.statement().size() > 1) {
            visit(ctx.statement(1));
        }

        instructions.add(endLabel);
        return null;
    }

    @Override
    public String visitWhileStmt(MiniCParser.WhileStmtContext ctx) {

        IRLabel start = new IRLabel();
        IRLabel end = new IRLabel();

        instructions.add(start);

        String cond = visit(ctx.expr());
        instructions.add(new IRIfZ(cond, end));

        visit(ctx.statement());

        instructions.add(new IRGoto(start));
        instructions.add(end);

        return null;
    }

    @Override
    public String visitForStmt(MiniCParser.ForStmtContext ctx) {

        // forInit
        if (ctx.forInit() != null) {
            if (ctx.forInit().assignStmt() != null) {
                visit(ctx.forInit().assignStmt());
            } else if (ctx.forInit().expr() != null) {
                visit(ctx.forInit().expr());
            }
        }

        IRLabel start = new IRLabel();
        IRLabel end = new IRLabel();

        instructions.add(start);

        // forCond
        if (ctx.forCond() != null && ctx.forCond().expr() != null) {
            String cond = visit(ctx.forCond().expr());
            instructions.add(new IRIfZ(cond, end));
        }

        // cuerpo
        visit(ctx.statement());

        // forStep
        // forInit
        if (ctx.forStep() != null) {
            if (ctx.forStep().assignStmt() != null) {
                visit(ctx.forStep().assignStmt());
            } else if (ctx.forStep().expr() != null) {
                visit(ctx.forStep().expr());
            }
        }

        instructions.add(new IRGoto(start));
        instructions.add(end);

        return null;
    }
}
