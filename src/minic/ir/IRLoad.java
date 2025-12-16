package minic.ir;

public class IRLoad extends IRInstr {

    public IRTemp dst;
    public String addr;

    public IRLoad(IRTemp dst, String addr) {
        this.dst = dst;
        this.addr = addr;
    }

    @Override
    public String toString() {
        return dst + " = *" + addr;
    }
}
