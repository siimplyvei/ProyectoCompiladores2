package minic.ir;

public class IRIfZ extends IRInstr {

    public final String condition;
    public final IRLabel target;

    public IRIfZ(String condition, IRLabel target) {
        this.condition = condition;
        this.target = target;
    }

    @Override
    public String toString() {
        return "ifz " + condition + " goto " + target.name;
    }
}
