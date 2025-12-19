package minic.ir;

import java.util.*;

public class IRStore extends IRInstr {

    public String addr;
    public String value;
    public String index;

    public IRStore(String addr, String value) {
        this.addr = addr;
        this.value = value;
    }

    @Override
    public void replaceUses(Map<String, String> copies) {
        addr  = repl(addr, copies);
        value = repl(value, copies);
    }

    @Override
    public String toString() {
        return "*" + addr + " = " + value;
    }
}
