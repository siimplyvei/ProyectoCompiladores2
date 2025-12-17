package minic.ir;

import java.util.*;

public class IRAssign extends IRInstr {

    public final String target;
    public String value;

    public IRAssign(String target, String value) {
        this.target = target;
        this.value = value;
    }

    @Override
    public void replaceUses(Map<String, String> copies) {
        value = repl(value, copies);
    }

    @Override
    public String toString() {
        return target + " = " + value;
    }
}
