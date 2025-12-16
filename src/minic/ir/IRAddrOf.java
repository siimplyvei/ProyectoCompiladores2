package minic.ir;

public class IRAddrOf extends IRInstr {

    public IRTemp dst;
    public String name;

    public IRAddrOf(IRTemp dst, String name) {
        this.dst = dst;
        this.name = name;
    }

    @Override
    public String toString() {
        return dst + " = &" + name;
    }
}
