package minic.semantic;

import java.util.HashMap;
import java.util.Map;

public class Scope {

    private Scope parent;
    private Map<String, Symbol> symbols = new HashMap<>();

    public Scope(Scope parent) {
        this.parent = parent;
    }

    public Scope getParent() {
        return parent;
    }

    public boolean define(Symbol symbol) {
        if (symbols.containsKey(symbol.getName())) {
            return false;
        }
        symbols.put(symbol.getName(), symbol);
        return true;
    }

    public Symbol resolve(String name) {
        Symbol s = symbols.get(name);
        if (s != null) {
            return s;
        }
        if (parent != null) {
            return parent.resolve(name);
        }
        return null;
    }
}
