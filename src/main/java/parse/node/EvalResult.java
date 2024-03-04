package parse.node;

import java.util.List;


public interface EvalResult {

    int asInt();

    long asLong();

    float asFloat();

    double asDouble();

    boolean asBoolean();

    String asString();

    Node asNode();

    List<?> asList();

    Object asObject();

    ResultType resultType();

    String langType();

    boolean isRefEqualTo(EvalResult other);

    record NodeResult(Node value) implements EvalResult {
        public int asInt() { return 1; }

        public long asLong() { return 1; }

        public float asFloat() { return 1; }

        public double asDouble() { return 1; }

        public boolean asBoolean() { return false; }

        public String asString() { return value.toString(); }

        public Object asObject() { return value; }

        public Node asNode() { return value; }

        public ResultType resultType() { return ResultType.NODE; }

        public String langType() { return value().getClass().getTypeName(); }

        public List<Node> asList() { return List.of(value); }

        public boolean isRefEqualTo(EvalResult other) {
            return value == other.asNode();
        }
    }

}
