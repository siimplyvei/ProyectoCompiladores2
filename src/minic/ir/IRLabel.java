package minic.ir;

public class IRLabel extends IRInstr {

    private static int count = 0;
    public final String name;

    public IRLabel() {
        this.name = "L" + (++count);
    }

    @Override
    public String toString() {
        return name + ":";
    }
}
