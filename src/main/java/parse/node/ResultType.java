package parse.node;

public enum ResultType {
    INT(SubResultType.NUMBER),
    LONG(SubResultType.NUMBER),
    FLOAT(SubResultType.NUMBER),
    DOUBLE(SubResultType.NUMBER),
    STRING(SubResultType.STRING),
    BOOLEAN(SubResultType.BOOLEAN),
    LAMBDA(SubResultType.OBJECT),
    OBJECT(SubResultType.OBJECT),
    QUOTE(SubResultType.STRING),
    NULL(SubResultType.OBJECT),
    ALIST(SubResultType.OBJECT),
    LIST(SubResultType.OBJECT),
    NODE(SubResultType.OBJECT),
    VOID(SubResultType.OBJECT);


    public final SubResultType subType;

    ResultType(SubResultType subType) { this.subType = subType; }

    public enum SubResultType {
        NUMBER,
        STRING,
        OBJECT,
        BOOLEAN,
        VOID,
        LAMBDA;
    }
}
