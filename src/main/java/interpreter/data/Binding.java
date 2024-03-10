package interpreter.data;

import parse.node.LiteralNode;

import java.util.StringJoiner;


public class Binding {
    private Class<?> type;
    private LiteralNode value;
    private final boolean dynamic;
    private final boolean mutable;

    public Binding(Class<?> type, LiteralNode value, boolean dynamic, boolean mutable) {
        this.type = type;
        this.value = value;
        this.dynamic = dynamic;
        this.mutable = mutable;
    }

    public static Binding ofDynamic(LiteralNode value) {
        return new Binding(value.classType(), value, true, true);
    }

    public static Binding ofMutable(LiteralNode value) {
        return new Binding(value.classType(), value, false, true);
    }

    public static Binding ofFinal(LiteralNode value) {
        return new Binding(value.classType(), value, false, false);
    }

    public LiteralNode reAssign(LiteralNode value) {
        if (!mutable) { throw new IllegalStateException("Reassignment of final value"); }
        if (!dynamic && !type.equals(value.classType())) { throw new IllegalStateException("Type mismatch"); }
        this.type = value.classType();
        this.value = value;
        return value;
    }

    public Class<?> type() {
        return type;
    }

    public LiteralNode value() {
        return value;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public boolean isMutable() {
        return mutable;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Binding.class.getSimpleName() + "[", "]")
                .add("type='" + type + "'")
                .add("value=" + value)
                .add("dynamic=" + dynamic)
                .add("mutable=" + mutable)
                .toString();
    }
}
