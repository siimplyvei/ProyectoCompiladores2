package minic.semantic;

import minic.*;
import org.antlr.v4.runtime.tree.ParseTree;

public class SemanticVisitor extends MiniCBaseVisitor<String> {

    private ScopedSymbolTable symtab = new ScopedSymbolTable();
    private String currentFunctionReturnType = null;

    // -------- PROGRAMA --------

    @Override
    public String visitProgram(MiniCParser.ProgramContext ctx) {
        symtab.enterScope(); // global
        visitChildren(ctx);
        symtab.exitScope();
        return null;
    }

    // -------- FUNCIONES --------

    @Override
    public String visitFuncDef(MiniCParser.FuncDefContext ctx) {
        String returnType = ctx.typeSpecifier().getText();
        String name = ctx.Identifier().getText();

        FunctionSymbol func = new FunctionSymbol(name, returnType);

        if (!symtab.define(func)) {
            error("Función redeclarada: " + name);
        }

        currentFunctionReturnType = returnType;

        symtab.enterScope(); // scope función

        if (ctx.params() != null) {
            visit(ctx.params());
        }

        visit(ctx.compoundStmt());

        symtab.exitScope();
        currentFunctionReturnType = null;
        return null;
    }

    @Override
    public String visitParam(MiniCParser.ParamContext ctx) {
        String type = ctx.typeSpecifier().getText();
        String name = ctx.declarator().getText();

        VariableSymbol param = new VariableSymbol(name, type);
        if (!symtab.define(param)) {
            error("Parámetro redeclarado: " + name);
        }
        return null;
    }

    // -------- DECLARACIONES --------

    @Override
    public String visitDeclaration(MiniCParser.DeclarationContext ctx) {
        String type = ctx.typeSpecifier().getText();

        for (MiniCParser.DeclaratorContext d : ctx.declaratorList().declarator()) {
            String name = d.getText();
            VariableSymbol var = new VariableSymbol(name, type);

            if (!symtab.define(var)) {
                error("Variable redeclarada: " + name);
            }
        }
        return null;
    }

    // -------- ASIGNACIONES --------

    @Override
    public String visitAssignStmt(MiniCParser.AssignStmtContext ctx) {
        String varName = ctx.lvalue().Identifier().getText();

        Symbol s = symtab.resolve(varName);
        if (s == null) {
            error("Variable no declarada: " + varName);
        }

        visit(ctx.expr());
        return null;
    }

    // -------- RETORNO --------

    @Override
    public String visitReturnStmt(MiniCParser.ReturnStmtContext ctx) {
        if (currentFunctionReturnType == null) {
            error("Return fuera de una función");
        }

        if (ctx.expr() == null && !currentFunctionReturnType.equals("void")) {
            error("Función debe retornar un valor");
        }

        return null;
    }

    // -------- USO DE VARIABLES --------

    @Override
    public String visitLvalue(MiniCParser.LvalueContext ctx) {
        String name = ctx.Identifier().getText();
        Symbol s = symtab.resolve(name);

        if (s == null) {
            error("Variable no declarada: " + name);
        }
        return s.getType();
    }

    // -------- UTIL --------

    private void error(String msg) {
        throw new RuntimeException("ERROR SEMÁNTICO: " + msg);
    }
}
