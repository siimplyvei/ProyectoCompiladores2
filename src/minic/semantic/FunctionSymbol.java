package minic.semantic;

import java.util.ArrayList;
import java.util.List;

public class FunctionSymbol extends Symbol {

    private List<VariableSymbol> parameters = new ArrayList<>();

    public FunctionSymbol(String name, String returnType) {
        super(name, returnType);
    }

    public void addParameter(VariableSymbol param) {
        parameters.add(param);
    }

    public List<VariableSymbol> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "Function(" + name + " -> " + type + ")";
    }
}
