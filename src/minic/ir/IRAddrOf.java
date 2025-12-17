package minic.ir;

import java.util.*;

public class IRAddrOf extends IRInstr {

    public IRTemp dst;
    public String name;

    public IRAddrOf(IRTemp dst, String name) {
        this.dst = dst;
        this.name = name;
    }

    @Override
    public void replaceUses(Map<String, String> copies) {
        // NO tocar name
    }

    @Override
    public String toString() {
        return dst + " = &" + name;
    }
}
