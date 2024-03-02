package parse.node;

import parse.token.TokenType;

import java.util.List;


public sealed interface OperationNode extends Node {

    record ArithmeticOp(TokenType.Operation operation, List<Node> operands) implements OperationNode { }

    record BooleanOp(TokenType.Operation operation, List<Node> operands) implements OperationNode { }

    record ComparisonOp(TokenType.Operation operation, List<Node> operands) implements OperationNode { }

    record EqualityOp(TokenType.Operation operation, List<Node> operands) implements OperationNode { }

    record UnaryOp(TokenType.Operation operation, Node operand) implements OperationNode { }

}
/* Math */

//
//    record AddOp(List<Node> operands) implements OperationNode { }
//
//    record SubtractOp(List<Node> operands) implements OperationNode { }
//
//    record MultiplyOp(List<Node> operands) implements OperationNode { }
//
//    record DivideOp(List<Node> operands) implements OperationNode { }
//
//    record ModuloOp(List<Node> operands) implements OperationNode { }
//
//    record ExponentiateOp(List<Node> operands) implements OperationNode { }
//
//    record IncOp(List<Node> operands) implements OperationNode { }
//
//    record DecOp(List<Node> operands) implements OperationNode { }
//
//    /* Boolean */
//    record OrOp(List<Node> operands) implements OperationNode { }
//
//    record AndOp(List<Node> operands) implements OperationNode { }
//
//    record XorOp(List<Node> operands) implements OperationNode { }
//
//    record NandOp(List<Node> operands) implements OperationNode { }
//
//    record NegateOp(List<Node> operands) implements OperationNode { }
//
/* Comparison */
//
//    record GreaterThanOp(List<Node> operands) implements OperationNode { }
//
//    record GreaterThanEqualOp(List<Node> operands) implements OperationNode { }
//
//    record LessThanOp(List<Node> operands) implements OperationNode { }
//
//    record LessThanEqualOp(List<Node> operands) implements OperationNode { }
//
//    record EqualityOp(List<Node> operands) implements OperationNode { }
//
//    record RefEqualityOp(List<Node> operands) implements OperationNode { }
//
//    record RefNonEqualityOp(List<Node> operands) implements OperationNode { }
//
//    record AssignOp(String identity, Node value) implements OperationNode { }
//
//}
