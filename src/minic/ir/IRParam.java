package minic.ir;

public class IRParam extends IRInstr {
    public final String value;

    public IRParam(String value) {
        this.value = value;
    }

    public String toString() {
        return "param " + value;
    }
}
