package minic.ir;

import java.util.*;

public class IRParam extends IRInstr {
    public String value;

    public IRParam(String value) {
        this.value = value;
    }

    @Override
    public void replaceUses(Map<String, String> copies) {
        value = repl(value, copies);
    }

    public String toString() {
        return "param " + value;
    }
}
