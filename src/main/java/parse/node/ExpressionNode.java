package parse.node;



import java.util.List;


public sealed interface ExpressionNode extends Node {

    record AssignOp(String identity, Node value) implements ExpressionNode { }

    record MultiExpr(List<Node> expressions) implements ExpressionNode { }

    record PrintExpr(Node value) implements ExpressionNode { }

    record IfExpr(CondBranch condBranch, Node elseBranch) implements ExpressionNode {
        public boolean hasElse() { return elseBranch != null; }
    }

    record CondBranch(Node condNode, Node thenNode) implements ExpressionNode { }

    record CondExpr(List<CondBranch> condBranches, Node elseBranch) implements ExpressionNode {
        public boolean hasElse() { return elseBranch != null; }
    }

   // record ForIExpr(int start, int end, int progAmount, CollectionNode collection, Node body) implements ExpressionNode { }

   // record ForEachExpr(CollectionNode collection, Node body) implements ExpressionNode { }

    record While(Node condition, Node body) implements ExpressionNode { }

    record ConsExpr(Node car, Node cdr) implements ExpressionNode { }

    record VariableAccess(String name) implements ExpressionNode { }

    record FunctionCall(String name, List<FuncArg> arguments) implements ExpressionNode { }

    record FuncArg(Node value, String name) implements ExpressionNode {
        public boolean isNamed() { return name != null; }
    }

}
