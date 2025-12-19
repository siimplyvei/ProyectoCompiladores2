package minic.mips;

import java.util.HashMap;
import java.util.Map;

public class StackFrame {

    private int offset = 0;
    private final Map<String, Integer> locals = new HashMap<>();
    private final Map<String, Boolean> isArrayMap = new HashMap<>(); // <- nuevo

    // Reserva espacio para una variable local o array
    public int allocate(String name, int size) {
        if (locals.containsKey(name)) return locals.get(name);
        offset -= size;
        locals.put(name, offset);

        // marcar como array si el tamaño > 4 bytes
        isArrayMap.put(name, size > 4);
        return offset;
    }

    // Reserva espacio para variable simple
    public int allocate(String name) {
        return allocate(name, 4);
    }

    // Devuelve el offset de la variable
    public int getOffset(String name) {
        return locals.get(name);
    }

    // Devuelve tamaño del frame
    public int getFrameSize() {
        return -offset;
    }

    // NUEVO: Indica si la variable es un array
    public boolean isArray(String name) {
        return isArrayMap.getOrDefault(name, false);
    }
}