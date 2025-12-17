package minic.ir;

import java.util.*;

public abstract class IRInstr {

    public void replaceUses(Map<String, String> copies) {
        // default: nada
    }

    protected String repl(String v, Map<String, String> copies) {
        while (copies.containsKey(v)) {
            v = copies.get(v);
        }
        return v;
    }
}
