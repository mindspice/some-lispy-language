package parse.node;

import java.util.List;


public sealed interface CollectionNode extends Node {
    record ArrayNode(String type, int size, List<Node> elements) implements CollectionNode { }

    record ConsLists(ExpressionNode.ConsExpr rootCell) implements CollectionNode { }

}
