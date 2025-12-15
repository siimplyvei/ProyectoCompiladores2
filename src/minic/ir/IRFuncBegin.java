package minic.ir;

public class IRFuncBegin extends IRInstr {
    public final String name;

    public IRFuncBegin(String name) {
        this.name = name;
    }

    public String toString() {
        return "func " + name;
    }
}
