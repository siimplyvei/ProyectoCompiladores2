package minic.ir;

public class IRStore extends IRInstr {

    public String addr;
    public String value;

    public IRStore(String addr, String value) {
        this.addr = addr;
        this.value = value;
    }

    @Override
    public String toString() {
        return "*" + addr + " = " + value;
    }
}
