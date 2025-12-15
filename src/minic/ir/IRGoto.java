package minic.ir;

public class IRGoto extends IRInstr {

    public final IRLabel target;

    public IRGoto(IRLabel target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return "goto " + target.name;
    }
}
