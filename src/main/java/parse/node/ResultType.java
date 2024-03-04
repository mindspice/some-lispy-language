package parse.node;

public enum ResultType {
    INT(SubResultType.NUMBER),
    LONG(SubResultType.NUMBER),
    FLOAT(SubResultType.NUMBER),
    DOUBLE(SubResultType.NUMBER),
    STRING(SubResultType.STRING),
    BOOLEAN(SubResultType.BOOLEAN),
    OBJECT(SubResultType.OBJECT),
    QUOTE(SubResultType.STRING),
    NULL(SubResultType.OBJECT),
    LIST(SubResultType.OBJECT),
    NODE(SubResultType.OBJECT);

    public final SubResultType subType;

    ResultType(SubResultType subType) { this.subType = subType; }

    public enum SubResultType {
        NUMBER,
        STRING,
        OBJECT,
        BOOLEAN,
    }
}
