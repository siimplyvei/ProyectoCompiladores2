package minic.ir;

import java.util.*;

public class IRReturn extends IRInstr {

    public String value;

    public IRReturn(String value) {
        this.value = value;
    }

    @Override
    public void replaceUses(Map<String, String> copies) {
        value = repl(value, copies);
    }

    @Override
    public String toString() {
        return "return " + value;
    }
}
