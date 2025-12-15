package minic.ir;

public class IRAssign extends IRInstr {

    public final String target;
    public final String value;

    public IRAssign(String target, String value) {
        this.target = target;
        this.value = value;
    }

    @Override
    public String toString() {
        return target + " = " + value;
    }
}
