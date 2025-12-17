package minic.semantic;

import java.util.Collection;

public class ScopedSymbolTable {

    private Scope currentScope;

    // ✅ CONSTRUCTOR: crea el scope global
    public ScopedSymbolTable() {
        currentScope = new Scope(null); // scope global
    }

    public void enterScope() {
        currentScope = new Scope(currentScope);
    }

    public void exitScope() {
        currentScope = currentScope.getParent();
    }

    public boolean define(Symbol symbol) {
        return currentScope.define(symbol);
    }

    public Symbol resolve(String name) {
        return currentScope.resolve(name);
    }

    public Scope getCurrentScope() {
        return currentScope;
    }

    public Symbol lookup(String name) {
        return resolve(name);
    }

    public boolean isGlobalScope() {
        return currentScope.getParent() == null;
    }
    public Collection<Symbol> getSymbols() {
        return currentScope.getSymbols();
    }
}