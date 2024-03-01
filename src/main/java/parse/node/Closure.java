package parse.node;

public record Closure(
        String refName,
        Node refClone
) {
    boolean isDynamic(){
        return refClone == null;
    }

    public Closure ofDynamic(String refName) {
        return new Closure(refName, null);
    }

    // TODO implement recursive cloning of reference node
    public Closure ofStatic(String refName, Node refNode) {
        return new Closure(refName, refNode);
    }

}
