package parse.node;

import java.util.List;


public sealed interface Node permits DefinitionNode, LiteralNode, OperationNode, ExpressionNode, Node.Program {
    record Program(List<Node> topMost) implements Node {
    }

}
