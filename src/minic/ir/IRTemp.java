package minic.ir;

public class IRTemp {
    private static int count = 0;
    public final String name;

    public IRTemp() {
        name = "t" + (++count);
    }

    @Override
    public String toString() {
        return name;
    }
}
