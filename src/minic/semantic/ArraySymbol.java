package minic.semantic;

import java.util.List;

public class ArraySymbol extends Symbol {

    private List<Integer> dimensions;

    public ArraySymbol(String name, String type, List<Integer> dimensions) {
        super(name, type);
        this.dimensions = dimensions;
    }

    public List<Integer> getDimensions() {
        return dimensions;
    }

    public int getDimensionCount() {
        return dimensions.size();
    }

    public int getSize(int dim) {
        return dimensions.get(dim);
    }
}
