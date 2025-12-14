package minic.semantic;

public class VariableSymbol extends Symbol {

    public VariableSymbol(String name, String type) {
        super(name, type);
    }

    @Override
    public String toString() {
        return "Variable(" + name + ":" + type + ")";
    }
}
