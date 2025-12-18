package minic.ast;

import minic.MiniCBaseVisitor;
import minic.MiniCParser;

public class ASTPrinter extends MiniCBaseVisitor<Void> {

    private int indent = 0;

    private void print(String s) {
        System.out.println("  ".repeat(indent) + s);
    }

    private void enter(String s) {
        print(s);
        indent++;
    }

    private void exit() {
        indent--;
    }

    // ================= PROGRAM =================

    @Override
    public Void visitProgram(MiniCParser.ProgramContext ctx) {
        enter("Program");
        visitChildren(ctx);
        exit();
        return null;
    }

    // ================= GLOBAL VAR =================

    @Override
    public Void visitGlobalDecl(MiniCParser.GlobalDeclContext ctx) {
        String type = ctx.typeSpecifier().getText();
        ctx.declaratorList().declarator()
                .forEach(d -> print("GlobalVar: " + type + " " + d.getText()));
        return null;
    }

    // ================= FUNCTION =================

    @Override
    public Void visitFuncDef(MiniCParser.FuncDefContext ctx) {
        String type = ctx.typeSpecifier().getText();
        String name = ctx.Identifier().getText();

        enter("Function: " + name + " : " + type);

        if (ctx.paramList() != null) {
            enter("Params");
            ctx.paramList().param().forEach(p ->
                    print(p.typeSpecifier().getText() + " " + p.Identifier().getText())
            );
            exit();
        }

        visit(ctx.compoundStmt());
        exit();
        return null;
    }

    // ================= BLOCK =================

    @Override
    public Void visitCompoundStmt(MiniCParser.CompoundStmtContext ctx) {
        enter("Block");
        visitChildren(ctx);
        exit();
        return null;
    }

    // ================= DECLARATION =================

    @Override
    public Void visitDeclaration(MiniCParser.DeclarationContext ctx) {
        String type = ctx.typeSpecifier().getText();
        ctx.declaratorList().declarator()
                .forEach(d -> print("VarDecl: " + type + " " + d.getText()));
        return null;
    }

    // ================= ASSIGN =================

    @Override
    public Void visitAssignStmt(MiniCParser.AssignStmtContext ctx) {
        enter("Assign");
        visit(ctx.lvalue());
        visit(ctx.expr());
        exit();
        return null;
    }

    // ================= LVALUE / ARRAYS =================

    @Override
    public Void visitLvalue(MiniCParser.LvalueContext ctx) {

        if (ctx.getChildCount() == 1) {
            print("Var: " + ctx.Identifier().getText());
        }
        else if (ctx.getChildCount() == 4) {
            enter("Array1D: " + ctx.Identifier().getText());
            visit(ctx.expr(0));
            exit();
        }
        else if (ctx.getChildCount() == 7) {
            enter("Array2D: " + ctx.Identifier().getText());

            enter("Index 1");
            visit(ctx.expr(0));
            exit();

            enter("Index 2");
            visit(ctx.expr(1));
            exit();

            exit();
        }
        return null;
    }

    // ================= IF =================

    @Override
    public Void visitIfStmt(MiniCParser.IfStmtContext ctx) {
        enter("If");

        enter("Condition");
        visit(ctx.expr());
        exit();

        enter("Then");
        visit(ctx.statement(0));
        exit();

        if (ctx.statement().size() == 2) {
            enter("Else");
            visit(ctx.statement(1));
            exit();
        }

        exit();
        return null;
    }

    // ================= WHILE =================

    @Override
    public Void visitWhileStmt(MiniCParser.WhileStmtContext ctx) {
        enter("While");

        enter("Condition");
        visit(ctx.expr());
        exit();

        visit(ctx.statement());
        exit();
        return null;
    }

    // ================= FOR =================

    @Override
    public Void visitForStmt(MiniCParser.ForStmtContext ctx) {
        enter("For");

        enter("Init");
        if (ctx.forInit() != null) visit(ctx.forInit());
        exit();

        enter("Condition");
        if (ctx.forCond() != null) visit(ctx.forCond());
        exit();

        enter("Step");
        if (ctx.forStep() != null) visit(ctx.forStep());
        exit();

        enter("Body");
        visit(ctx.statement());
        exit();

        exit();
        return null;
    }

    // ================= RETURN =================

    @Override
    public Void visitReturnStmt(MiniCParser.ReturnStmtContext ctx) {
        enter("Return");
        if (ctx.expr() != null) visit(ctx.expr());
        exit();
        return null;
    }

    // ================= CALL =================

    @Override
    public Void visitPrimary(MiniCParser.PrimaryContext ctx) {

        if (ctx.Identifier() != null && ctx.getChildCount() >= 3) {
            enter("Call: " + ctx.Identifier().getText());
            if (ctx.argList() != null) {
                ctx.argList().expr().forEach(this::visit);
            }
            exit();
        }
        else if (ctx.IntegerConst() != null) {
            print("Int: " + ctx.IntegerConst().getText());
        }
        else if (ctx.CharConst() != null) {
            print("Char: " + ctx.CharConst().getText());
        }
        else if (ctx.StringLiteral() != null) {
            print("String: " + ctx.StringLiteral().getText());
        }
        else if (ctx.getText().equals("true") || ctx.getText().equals("false")) {
            print("Bool: " + ctx.getText());
        }
        else if (ctx.lvalue() != null) {
            visit(ctx.lvalue());
        }
        else if (ctx.expr() != null) {
            visit(ctx.expr());
        }

        return null;
    }

    // ================= EXPRESSIONS =================

    @Override
    public Void visitAdditiveExpr(MiniCParser.AdditiveExprContext ctx) {
        if (ctx.getChildCount() == 3) {
            enter("Add/Sub: " + ctx.getChild(1).getText());
            visit(ctx.getChild(0));
            visit(ctx.getChild(2));
            exit();
            return null;
        }
        return visitChildren(ctx);
    }

    @Override
    public Void visitMultiplicativeExpr(MiniCParser.MultiplicativeExprContext ctx) {
        if (ctx.getChildCount() == 3) {
            enter("Mul/Div/Mod: " + ctx.getChild(1).getText());
            visit(ctx.getChild(0));
            visit(ctx.getChild(2));
            exit();
            return null;
        }
        return visitChildren(ctx);
    }

    @Override
    public Void visitRelationalExpr(MiniCParser.RelationalExprContext ctx) {
        if (ctx.getChildCount() == 3) {
            enter("RelOp: " + ctx.getChild(1).getText());
            visit(ctx.getChild(0));
            visit(ctx.getChild(2));
            exit();
            return null;
        }
        return visitChildren(ctx);
    }

    @Override
    public Void visitEqualityExpr(MiniCParser.EqualityExprContext ctx) {
        if (ctx.getChildCount() == 3) {
            enter("EqOp: " + ctx.getChild(1).getText());
            visit(ctx.getChild(0));
            visit(ctx.getChild(2));
            exit();
            return null;
        }
        return visitChildren(ctx);
    }

    @Override
    public Void visitLogicalAndExpr(MiniCParser.LogicalAndExprContext ctx) {
        if (ctx.getChildCount() == 3) {
            enter("And");
            visit(ctx.getChild(0));
            visit(ctx.getChild(2));
            exit();
            return null;
        }
        return visitChildren(ctx);
    }

    @Override
    public Void visitLogicalOrExpr(MiniCParser.LogicalOrExprContext ctx) {
        if (ctx.getChildCount() == 3) {
            enter("Or");
            visit(ctx.getChild(0));
            visit(ctx.getChild(2));
            exit();
            return null;
        }
        return visitChildren(ctx);
    }
}
