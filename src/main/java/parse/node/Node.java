package parse.node;

import java.util.List;


public sealed interface Node permits CollectionNode, DefinitionNode, ExpressionNode, LiteralNode, Node.program, OperationNode {

    record program(List<Node> topMost) implements Node { }

}
