package minic.mips;

import java.util.HashMap;
import java.util.Map;

public class StackFrame {

    private int offset = 0;
    private final Map<String, Integer> locals = new HashMap<>();

    // Reserva espacio para una variable local
    public int allocate(String name) {
        if (locals.containsKey(name)) return locals.get(name);
        offset -= 4;
        locals.put(name, offset);
        return offset;
    }

    public int getOffset(String name) {
        return locals.get(name);
    }

    public int getFrameSize() {
        return -offset;
    }
}
