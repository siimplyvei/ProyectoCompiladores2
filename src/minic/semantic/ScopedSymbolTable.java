package minic.semantic;

public class ScopedSymbolTable {

    private Scope currentScope;

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
}
