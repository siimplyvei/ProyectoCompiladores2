package minic.mips;

import java.util.HashMap;
import java.util.Map;

public class StackFrame {

    private int offset = 0;
    private final Map<String, Integer> locals = new HashMap<>();

    // Reserva espacio para una variable local
    public int allocate(String name) {
        if (!locals.containsKey(name)) {
            offset -= 4; // stack crece hacia abajo
            locals.put(name, offset);
        }
        return locals.get(name);
    }

    public int getOffset(String name) {
        return locals.get(name);
    }

    public int getFrameSize() {
        return -offset;
    }
}
