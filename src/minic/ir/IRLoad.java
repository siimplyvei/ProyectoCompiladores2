package minic.ir;

import java.util.*;

public class IRLoad extends IRInstr {

    public IRTemp dst;
    public String addr;

    public IRLoad(IRTemp dst, String addr) {
        this.dst = dst;
        this.addr = addr;
    }

    @Override
    public void replaceUses(Map<String, String> copies) {
        addr = repl(addr, copies);
    }

    @Override
    public String toString() {
        return dst + " = *" + addr;
    }
}
