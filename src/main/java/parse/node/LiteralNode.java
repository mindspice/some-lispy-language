package parse.node;

// TODO should add bytes and char, etc to parser also should add bignums

public sealed interface LiteralNode extends Node {

    record IntLit(int value) implements LiteralNode { }

    record FloatLit(float value) implements LiteralNode { }

    record LongLit(long value) implements LiteralNode { }

    record DoubleLit(double value) implements LiteralNode { }

    record StringLit(String value) implements LiteralNode { }

    record QuoteLit(String value) implements LiteralNode { }

    record BooleanLit(boolean value) implements LiteralNode { }

    record ObjectLit( Object value) implements LiteralNode { }

    record NullLit() implements LiteralNode { }

}
