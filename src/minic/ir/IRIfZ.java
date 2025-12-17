package minic.ir;

import java.util.*;

public class IRIfZ extends IRInstr {

    public String condition;
    public final IRLabel target;

    public IRIfZ(String condition, IRLabel target) {
        this.condition = condition;
        this.target = target;
    }

    public void replaceUses(Map<String, String> copies) {
        condition = repl(condition, copies);
    }

    @Override
    public String toString() {
        return "ifz " + condition + " goto " + target.name;
    }
}
