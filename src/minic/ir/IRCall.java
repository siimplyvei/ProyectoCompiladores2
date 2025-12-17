package minic.ir;

import java.util.*;

public class IRCall extends IRInstr {
    public final String funcName;
    public final int argc;
    public final IRTemp result; // puede ser null (void)

    public IRCall(String funcName, int argc, IRTemp result) {
        this.funcName = funcName;
        this.argc = argc;
        this.result = result;
    }

    @Override
    public void replaceUses(Map<String, String> copies) {
        // nada
    }

    public String toString() {
        if (result == null)
            return "call " + funcName + ", " + argc;
        return result + " = call " + funcName + ", " + argc;
    }
}
