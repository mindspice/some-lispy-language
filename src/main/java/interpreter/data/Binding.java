package interpreter.data;

public class Binding {
    private String type;
    private Object value;
    private final boolean dynamic;
    private final boolean mutable;

    public Binding(String type, Object value, boolean dynamic, boolean mutable) {
        this.type = type;
        this.value = value;
        this.dynamic = dynamic;
        this.mutable = mutable;
    }

    public static Binding ofDynamic(String type, Object value) {
        return new Binding(type, value, true, true);
    }

    public static Binding ofMutable(String type, Object value) {
        return new Binding(type, value, false, true);
    }

    public static Binding ofFinal(String type, Object value) {
        return new Binding(type, value, false, false);
    }

    public void reAssign(String type, Object value) {
        if (!mutable) { throw new IllegalStateException("Reassignment of final value"); }
        if (!dynamic && !type.equals(this.type)) { throw new IllegalStateException("Type mismatch"); }
        this.type = type;
        this.value = value;
    }

    public String type() {
        return type;
    }

    public Object value() {
        return value;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public boolean isMutable() {
        return mutable;
    }
}
