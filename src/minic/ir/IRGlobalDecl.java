package minic.ir;

public class IRGlobalDecl extends IRInstr {
    public final String name;
    public final int size;

    public IRGlobalDecl(String name, int size) {
        this.name = name;
        this.size = size;
    }

    @Override
    public String toString() {
        return "global " + name + " [" + size + "]";
    }
}
