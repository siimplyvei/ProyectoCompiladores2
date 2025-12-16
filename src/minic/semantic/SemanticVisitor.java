package minic.semantic;

import minic.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;
import java.util.ArrayList;

import minic.semantic.ArraySymbol;
import minic.semantic.VariableSymbol;
import minic.semantic.FunctionSymbol;

public class SemanticVisitor extends MiniCBaseVisitor<String> {

    private ScopedSymbolTable symtab = new ScopedSymbolTable();
    private String currentFunctionReturnType = null;
    private String currentType;

    // -------- PROGRAMA --------

    @Override
    public String visitProgram(MiniCParser.ProgramContext ctx) {

        // 1️⃣ Registrar variables globales
        for (MiniCParser.GlobalDeclContext g : ctx.globalDecl()) {
            visit(g);
        }

        // 2️⃣ Registrar funciones
        for (MiniCParser.FuncDefContext f : ctx.funcDef()) {
            visit(f);
        }

        return null;
    }

    @Override
    public String visitGlobalDecl(MiniCParser.GlobalDeclContext ctx) {

        String type = ctx.typeSpecifier().getText();

        for (MiniCParser.DeclaratorContext d : ctx.declaratorList().declarator()) {

            String name = d.Identifier().getText();

            if (d.IntegerConst().isEmpty()) {
                // variable simple
                VariableSymbol var = new VariableSymbol(name, type);
                if (!symtab.define(var)) {
                    error("Variable global redeclarada: " + name);
                }
            } else {
                // arreglo
                List<Integer> dims = new ArrayList<>();
                for (TerminalNode t : d.IntegerConst()) {
                    dims.add(Integer.parseInt(t.getText()));
                }

                ArraySymbol arr = new ArraySymbol(name, type, dims);
                if (!symtab.define(arr)) {
                    error("Arreglo global redeclarado: " + name);
                }
            }
        }
        return null;
    }

    // -------- FUNCIONES --------
    @Override
    public String visitTypeSpecifier(MiniCParser.TypeSpecifierContext ctx) {
        currentType = ctx.getText();
        return null;
    }

    @Override
    public String visitFuncDef(MiniCParser.FuncDefContext ctx) {

        String returnType = ctx.typeSpecifier().getText();
        String name = ctx.Identifier().getText();

        FunctionSymbol func = new FunctionSymbol(name, returnType);

        if (!symtab.define(func)) {
            error("Función redeclarada: " + name);
        }

        currentFunctionReturnType = returnType;

        // 🔽 scope de función
        symtab.enterScope();

        // parámetros (si existen)
        if (ctx.paramList() != null) {
            visit(ctx.paramList());
        }

        // cuerpo
        visit(ctx.compoundStmt());

        // 🔼 salir del scope
        symtab.exitScope();

        currentFunctionReturnType = null;
        return null;
    }

    @Override
    public String visitParam(MiniCParser.ParamContext ctx) {
        String name = ctx.Identifier().getText();
        // registrar parámetro en la tabla de símbolos
        return null;
    }
    // -------- DECLARACIONES --------

    @Override
    public String visitDeclaration(MiniCParser.DeclarationContext ctx) {

        String type = ctx.typeSpecifier().getText();

        for (MiniCParser.DeclaratorContext d : ctx.declaratorList().declarator()) {
            String name = d.Identifier().getText();

            if (d.IntegerConst().isEmpty()) {
                VariableSymbol var = new VariableSymbol(name, type);
                if (!symtab.define(var)) {
                    error("Variable redeclarada: " + name);
                }
            } else {
                List<Integer> dims = new ArrayList<>();
                for (TerminalNode t : d.IntegerConst()) {
                    dims.add(Integer.parseInt(t.getText()));
                }
                ArraySymbol arr = new ArraySymbol(name, type, dims);
                if (!symtab.define(arr)) {
                    error("Arreglo redeclarado: " + name);
                }
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

        Symbol sym = symtab.lookup(name);
        if (sym == null) {
            error("Variable no declarada: " + name);
        }

        // 🔹 Caso variable simple
        if (ctx.expr().isEmpty()) {
            if (sym instanceof ArraySymbol) {
                error("Uso de arreglo sin índices: " + name);
            }
            return sym.getType();
        }

        // 🔹 Caso arreglo
        if (!(sym instanceof ArraySymbol)) {
            error("Variable no es un arreglo: " + name);
        }

        ArraySymbol arr = (ArraySymbol) sym;

        int usedDims = ctx.expr().size();
        int declaredDims = arr.getDimensionCount();

        // ❌ demasiados índices
        if (usedDims > declaredDims) {
            error("Demasiados índices para arreglo " + name);
        }

        // ❌ índices insuficientes (opcional, pero recomendado)
        if (usedDims < declaredDims) {
            error("Faltan índices para arreglo " + name);
        }

        // 🔹 Validar cada índice
        for (MiniCParser.ExprContext e : ctx.expr()) {
            String t = visit(e);
            if (!t.equals("int")) {
                error("Índice de arreglo debe ser int");
            }
        }

        return arr.getType();
    }

    @Override
    public String visitCompoundStmt(MiniCParser.CompoundStmtContext ctx) {

        // 🔑 nuevo scope para el bloque
        symtab.enterScope();

        // declaraciones locales
        for (MiniCParser.DeclarationContext d : ctx.declaration()) {
            visit(d);
        }

        // sentencias
        for (MiniCParser.StatementContext s : ctx.statement()) {
            visit(s);
        }

        // salir del scope del bloque
        symtab.exitScope();

        return null;
    }

    // -------- UTIL --------

    private void error(String msg) {
        throw new RuntimeException("ERROR SEMÁNTICO: " + msg);
    }
}
