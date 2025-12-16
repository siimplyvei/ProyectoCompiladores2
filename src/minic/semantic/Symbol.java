package minic.semantic;

import java.util.*;

public class Symbol {
    public String name;
    public String type;
    public List<Integer> dimensions = new ArrayList<>();

    public Symbol(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public boolean isArray() {
        return !dimensions.isEmpty();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
