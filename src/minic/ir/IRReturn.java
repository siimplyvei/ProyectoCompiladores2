package minic.ir;

public class IRReturn extends IRInstr {

    public final String value;

    public IRReturn(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "return " + value;
    }
}
