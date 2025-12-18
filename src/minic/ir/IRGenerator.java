package minic.ir;

import minic.*;
import org.antlr.v4.runtime.tree.ParseTree;
import java.util.ArrayList;
import java.util.List;
import minic.semantic.Symbol;
import minic.semantic.ArraySymbol;
import minic.semantic.ScopedSymbolTable;

public class IRGenerator extends MiniCBaseVisitor<String> {
    private final List<IRInstr> instructions = new ArrayList<>();
    public List<IRInstr> getInstructions() { return instructions; }

    private int tempCount = 0;
    private String newTemp() { return "t" + (++tempCount); }
    private void emit(IRInstr instr) { instructions.add(instr); }

    private boolean isLvalue = false;
    private ScopedSymbolTable symtab;

    public IRGenerator(ScopedSymbolTable symtab) { this.symtab = symtab; }
    public ScopedSymbolTable getSymbolTable() { return symtab; }

    // ---------- EXPRESSIONS ----------
    @Override
    public String visitPrimary(MiniCParser.PrimaryContext ctx) {
        // ---------- llamada a función ----------
        if (ctx.Identifier() != null && ctx.getChildCount() >= 2 && ctx.getChild(1).getText().equals("(")) {
            String fname = ctx.Identifier().getText();

            // ===== BUILTINS =====
            if (fname.equals("print_str")) {
                String arg = visit(ctx.argList().expr(0));
                emit(new IRParam(arg));
                emit(new IRCall("__print_string", 1, null));
                return null;
            }
            if (fname.equals("print_int")) {
                String arg = visit(ctx.argList().expr(0));
                emit(new IRParam(arg));
                emit(new IRCall("__print_int", 1, null));
                return null;
            }
            if (fname.equals("print_char")) {
                String arg = visit(ctx.argList().expr(0));
                emit(new IRParam(arg));
                emit(new IRCall("__print_char", 1, null));
                return null;
            }
            if (fname.equals("println")) {
                emit(new IRCall("__print_newline", 0, null));
                return null;
            }

            // ===== FUNCIONES NORMALES =====
            int argc = 0;
            if (ctx.argList() != null) {
                for (MiniCParser.ExprContext e : ctx.argList().expr()) {
                    String arg = visit(e);
                    emit(new IRParam(arg));
                    argc++;
                }
            }
            IRTemp ret = new IRTemp();
            emit(new IRCall(fname, argc, ret));
            return ret.toString();
        }

        // ---------- lvalue (variables y arreglos) ----------
        if (ctx.lvalue() != null) {
            boolean old = isLvalue;
            isLvalue = false; // primary SIEMPRE es rvalue
            String v = visit(ctx.lvalue());
            isLvalue = old;
            return v;
        }

        // ---------- literales ----------
        if (ctx.IntegerConst() != null) { return ctx.IntegerConst().getText(); }
        if (ctx.StringLiteral() != null) { return ctx.StringLiteral().getText(); }
        if (ctx.CharConst() != null) { return ctx.CharConst().getText(); }

        // ---------- paréntesis ----------
        if (ctx.expr() != null) { return visit(ctx.expr()); }

        return visitChildren(ctx);
    }

    @Override
    public String visitLvalue(MiniCParser.LvalueContext ctx) {
        String name = ctx.Identifier().getText();
        Symbol sym = symtab.lookup(name);

        // variable simple
        if (ctx.expr().isEmpty()) {
            IRTemp addr = new IRTemp();
            emit(new IRAddrOf(addr, name));
            if (!isLvalue) {
                IRTemp val = new IRTemp();
                emit(new IRLoad(val, addr.toString()));
                return val.toString();
            }
            return addr.toString();
        }

        // ---- ARREGLOS ----
        if (!(sym instanceof ArraySymbol)) {
            throw new RuntimeException("ERROR IR: " + name + " no es un arreglo");
        }

        // 1D
        if (ctx.expr().size() == 1) {
            String index = visit(ctx.expr(0));
            IRTemp offset = new IRTemp();
            emit(new IRBinOp(offset, index, "*", "4"));
            IRTemp base = new IRTemp();
            emit(new IRAddrOf(base, name));
            IRTemp addr = new IRTemp();
            emit(new IRBinOp(addr, base.toString(), "+", offset.toString()));
            if (isLvalue) { return addr.toString(); }
            else {
                IRTemp val = new IRTemp();
                emit(new IRLoad(val, addr.toString()));
                return val.toString();
            }
        }

        // 2D
        if (ctx.expr().size() == 2) {
            String i = visit(ctx.expr(0));
            String j = visit(ctx.expr(1));
            ArraySymbol arr = (ArraySymbol) sym;
            int cols = arr.getSize(1);
            IRTemp t1 = new IRTemp();
            emit(new IRBinOp(t1, i, "*", String.valueOf(cols)));
            IRTemp t2 = new IRTemp();
            emit(new IRBinOp(t2, t1.toString(), "+", j));
            IRTemp t3 = new IRTemp();
            emit(new IRBinOp(t3, t2.toString(), "*", "4"));
            IRTemp base = new IRTemp();
            emit(new IRAddrOf(base, name));
            IRTemp addr = new IRTemp();
            emit(new IRBinOp(addr, base.toString(), "+", t3.toString()));
            if (isLvalue) { return addr.toString(); }
            else {
                IRTemp val = new IRTemp();
                emit(new IRLoad(val, addr.toString()));
                return val.toString();
            }
        }

        return null;
    }

    @Override
    public String visitDeclaration(MiniCParser.DeclarationContext ctx) {
        String name = ctx.declaratorList().declarator(0).Identifier().getText();
        Symbol sym = symtab.lookup(name);

        // 👇 SI ESTAMOS EN SCOPE GLOBAL
        if (symtab.isGlobalScope()) {
            int size = 4; // int simple
            if (sym instanceof ArraySymbol) {
                ArraySymbol arr = (ArraySymbol) sym;
                int total = 1;
                for (int d = 0; d < arr.getDimensionCount(); d++) total *= arr.getSize(d);
                size = total * 4;
            }
            instructions.add(new IRGlobalDecl(name, size));
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
        String rhs = visit(ctx.expr());
        isLvalue = true;
        String lhsAddr = visit(ctx.lvalue());
        isLvalue = false;
        emit(new IRStore(lhsAddr, rhs));
        return null;
    }

    @Override
    public String visitCompoundStmt(MiniCParser.CompoundStmtContext ctx) {
        symtab.enterScope(); // 👈 NUEVO BLOQUE
        for (MiniCParser.DeclarationContext d : ctx.declaration()) visit(d);
        for (MiniCParser.StatementContext s : ctx.statement()) visit(s);
        symtab.exitScope(); // 👈 FIN BLOQUE
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
        // Evaluar el primer término
        String result = visit(ctx.additiveExpr(0));

        // Recorrer operadores adicionales, generando temporales
        for (int i = 1; i < ctx.additiveExpr().size(); i++) {
            String right = visit(ctx.additiveExpr(i));
            String op = ctx.getChild(2 * i - 1).getText();

            // Crear un temporal para el resultado de esta operación relacional
            IRTemp temp = new IRTemp();

            // Añadir la instrucción IR de operación binaria
            instructions.add(new IRBinOp(temp, result, op, right));

            // Actualizar 'result' al temporal recién creado
            result = temp.toString();
        }

        // Devuelve el temporal que representa el valor de la expresión relacional
        return result;
    }

    @Override
    public String visitEqualityExpr(MiniCParser.EqualityExprContext ctx) {
        String result = visit(ctx.relationalExpr(0));
        for (int i = 1; i < ctx.relationalExpr().size(); i++) {
            String right = visit(ctx.relationalExpr(i));
            String op = ctx.getChild(2 * i - 1).getText();
            IRTemp temp = new IRTemp();
            emit(new IRBinOp(temp, result, op, right));
            result = temp.toString();
        }
        return result;
    }

    @Override
    public String visitWhileStmt(MiniCParser.WhileStmtContext ctx) {
        IRLabel start = new IRLabel();
        IRLabel end = new IRLabel();

        instructions.add(start);

        String cond = visit(ctx.expr());

        if (cond == null) {
            throw new RuntimeException("Condición while no evaluada");
        }

        instructions.add(new IRIfZ(cond, end));
        visit(ctx.statement());
        instructions.add(new IRGoto(start));
        instructions.add(end);

        return null;
    }

    @Override
    public String visitIfStmt(MiniCParser.IfStmtContext ctx) {
        String condExpr = visit(ctx.expr());

        if (condExpr.startsWith("&")) {
            IRTemp tmp = new IRTemp();
            emit(new IRLoad(tmp, condExpr));
            condExpr = tmp.toString();
        }

        IRLabel elseLabel = new IRLabel();
        IRLabel endLabel = new IRLabel();
        instructions.add(new IRIfZ(condExpr, elseLabel));
        visit(ctx.statement(0));
        instructions.add(new IRGoto(endLabel));
        instructions.add(elseLabel);
        if (ctx.statement().size() > 1) visit(ctx.statement(1));
        instructions.add(endLabel);
        return null;
    }

    @Override
    public String visitForStmt(MiniCParser.ForStmtContext ctx) {
        // forInit
        if (ctx.forInit() != null) {
            if (ctx.forInit().assignStmt() != null) visit(ctx.forInit().assignStmt());
            else if (ctx.forInit().expr() != null) visit(ctx.forInit().expr());
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
        if (ctx.forStep() != null) {
            if (ctx.forStep().assignStmt() != null) visit(ctx.forStep().assignStmt());
            else if (ctx.forStep().expr() != null) visit(ctx.forStep().expr());
        }

        instructions.add(new IRGoto(start));
        instructions.add(end);
        return null;
    }

    @Override
    public String visitFuncDef(MiniCParser.FuncDefContext ctx) {
        String fname = ctx.Identifier().getText();
        instructions.add(new IRFuncBegin(fname));
        symtab.enterScope();
        visit(ctx.compoundStmt());
        symtab.exitScope();
        instructions.add(new IRFuncEnd());
        return null;
    }
}