package parse.node;

import parse.token.TokenType;

import java.util.List;


public sealed interface OperationNode extends Node {
    OperationType opType();

    List<Node> operands();

    public enum OperationType {
        ARITHMETIC,
        BOOLEAN,
        COMPARISON,
        EQUALITY
    }

    static OperationNode getOperationNode(TokenType.Operation operation, List<Node> operands) {
        return switch (operation) {
            case AND -> new AndOp(OperationType.BOOLEAN, operands);
            case OR -> new OrOp(OperationType.BOOLEAN, operands);
            case XOR -> new XorOp(OperationType.BOOLEAN, operands);
            case NOR -> new NorOp(OperationType.BOOLEAN, operands);
            case XNOR -> new XNorOp(OperationType.BOOLEAN, operands);
            case NAND -> new NandOp(OperationType.BOOLEAN, operands);
            case NEGATE -> new NegateOp(OperationType.BOOLEAN, operands);
            case PLUS -> new AddOp(OperationType.ARITHMETIC, operands);
            case MINUS -> new SubtractOp(OperationType.ARITHMETIC, operands);
            case ASTERISK -> new MultiplyOp(OperationType.ARITHMETIC, operands);
            case SLASH -> new DivideOp(OperationType.ARITHMETIC, operands);
            case CARET -> new ExponentiateOp(OperationType.ARITHMETIC, operands);
            case PERCENT -> new ModuloOp(OperationType.ARITHMETIC, operands);
            case PLUS_PLUS -> new IncOp(OperationType.ARITHMETIC, operands);
            case MINUS_MINUS -> new DecOp(OperationType.ARITHMETIC, operands);
            case GREATER -> new GreaterThanOp(OperationType.COMPARISON, operands);
            case LESS -> new LessThanOp(OperationType.COMPARISON, operands);
            case GREATER_EQUAL -> new GreaterThanEqualOp(OperationType.COMPARISON, operands);
            case LESS_EQUAL -> new LessThanEqualOp(OperationType.COMPARISON, operands);
            case EQUALS -> new EqualityOp(OperationType.EQUALITY, operands);
            case BANG_EQUAL -> new RefNonEqualityOp(OperationType.EQUALITY, operands);
            case REF_EQUALS -> new RefEqualityOp(OperationType.EQUALITY, operands);
        };
    }


    /* Math */

    record AddOp(OperationType opType, List<Node> operands) implements OperationNode { }

    record SubtractOp(OperationType opType, List<Node> operands) implements OperationNode { }

    record MultiplyOp(OperationType opType, List<Node> operands) implements OperationNode { }

    record DivideOp(OperationType opType, List<Node> operands) implements OperationNode { }

    record ModuloOp(OperationType opType, List<Node> operands) implements OperationNode { }

    record ExponentiateOp(OperationType opType, List<Node> operands) implements OperationNode { }

    record IncOp(OperationType opType, List<Node> operands) implements OperationNode { }

    record DecOp(OperationType opType, List<Node> operands) implements OperationNode { }

    /* Boolean */
    record OrOp(OperationType opType, List<Node> operands) implements OperationNode { }

    record AndOp(OperationType opType, List<Node> operands) implements OperationNode { }

    record XorOp(OperationType opType, List<Node> operands) implements OperationNode { }

    record NorOp(OperationType opType, List<Node> operands) implements OperationNode { }

    record XNorOp(OperationType opType, List<Node> operands) implements OperationNode { }

    record NandOp(OperationType opType, List<Node> operands) implements OperationNode { }

    record NegateOp(OperationType opType, List<Node> operands) implements OperationNode { }

    /* Comparison */

    record GreaterThanOp(OperationType opType, List<Node> operands) implements OperationNode { }

    record GreaterThanEqualOp(OperationType opType, List<Node> operands) implements OperationNode { }

    record LessThanOp(OperationType opType, List<Node> operands) implements OperationNode { }

    record LessThanEqualOp(OperationType opType, List<Node> operands) implements OperationNode { }

    /* Equality */

    record EqualityOp(OperationType opType, List<Node> operands) implements OperationNode { }

    record RefEqualityOp(OperationType opType, List<Node> operands) implements OperationNode { }

    record RefNonEqualityOp(OperationType opType, List<Node> operands) implements OperationNode { }

}

