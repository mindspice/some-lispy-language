package parse.node;

// TODO should add bytes and char, etc to parser also should add bignums

import interpreter.Environment;
import interpreter.ScopeEnv;

import java.util.List;


public sealed interface LiteralNode extends Node, EvalResult {
    BooleanLit TRUE = new BooleanLit(true);
    BooleanLit FALSE = new BooleanLit(false);
    NullLit NULL = new NullLit();
    VoidLit VOID = new VoidLit();

    record IntLit(int value) implements LiteralNode, EvalResult {

        public boolean asBoolean() { return value != 0; }

        public String asString() { return String.valueOf(value); }

        @Override
        public String toString() { return String.valueOf(value); }

        public int asInt() { return value; }

        public long asLong() { return value; }

        public float asFloat() { return value; }

        public double asDouble() { return value; }

        public Object asObject() { return value; }

        public Node asNode() { return this; }

        public List<Integer> asList() { return List.of(value); }

        public ResultType resultType() { return ResultType.INT; }

        public String langType() { return "int"; }

        public boolean isRefEqualTo(EvalResult other) {
            if (other.resultType().subType != ResultType.SubResultType.NUMBER) {
                return false;
            }
            if (other.resultType() == this.resultType()) {
                return other.asInt() == value;
            } else if (other.resultType() == ResultType.DOUBLE) {
                return other.asDouble() == value;
            } else if (other.resultType() == ResultType.LONG) {
                return other.asLong() == value;
            } else {
                return other.asFloat() == value;
            }
        }
    }

    record FloatLit(float value) implements LiteralNode, EvalResult {

        public boolean asBoolean() { return value != 0; }

        public String asString() { return String.valueOf(value); }

        @Override
        public String toString() { return String.valueOf(value); }

        public int asInt() { return (int) value; }

        public long asLong() { return (long) value; }

        public float asFloat() { return value; }

        public double asDouble() { return value; }

        public Object asObject() { return value; }

        public Node asNode() { return this; }

        public List<Float> asList() { return List.of(value); }

        public ResultType resultType() { return ResultType.FLOAT; }

        public String langType() { return "float"; }

        public boolean isRefEqualTo(EvalResult other) {
            if (other.resultType().subType != ResultType.SubResultType.NUMBER) {
                return false;
            }
            if (other.resultType() == this.resultType()) {
                return other.asFloat() == value;
            } else if (other.resultType() == ResultType.INT) {
                return other.asInt() == value;
            } else if (other.resultType() == ResultType.DOUBLE) {
                return other.asDouble() == value;
            } else {
                return other.asLong() == value;
            }
        }
    }

    record LongLit(long value) implements LiteralNode, EvalResult {

        public boolean asBoolean() { return value != 0; }

        public String asString() { return String.valueOf(value); }

        @Override
        public String toString() { return String.valueOf(value); }

        public int asInt() { return (int) value; }

        public long asLong() { return value; }

        public float asFloat() { return value; }

        public double asDouble() { return value; }

        public Object asObject() { return value; }

        public Node asNode() { return this; }

        public List<Long> asList() { return List.of(value); }

        public ResultType resultType() { return ResultType.LONG; }

        public String langType() { return "long"; }

        public boolean isRefEqualTo(EvalResult other) {
            if (other.resultType().subType != ResultType.SubResultType.NUMBER) {
                return false;
            }
            if (other.resultType() == this.resultType()) {
                return other.asLong() == value;
            } else if (other.resultType() == ResultType.INT) {
                return other.asInt() == value;
            } else if (other.resultType() == ResultType.DOUBLE) {
                return other.asDouble() == value;
            } else {
                return other.asFloat() == value;
            }
        }
    }

    record DoubleLit(double value) implements LiteralNode, EvalResult {
        public boolean asBoolean() { return value != 0; }

        public String asString() { return String.valueOf(value); }

        @Override
        public String toString() { return String.valueOf(value); }

        public int asInt() { return (int) value; }

        public long asLong() { return (long) value; }

        public float asFloat() { return (float) value; }

        public double asDouble() { return value; }

        public Object asObject() { return value; }

        public Node asNode() { return this; }

        public List<Double> asList() { return List.of(value); }

        public ResultType resultType() { return ResultType.DOUBLE; }

        public String langType() { return "double"; }

        public boolean isRefEqualTo(EvalResult other) {
            if (other.resultType().subType != ResultType.SubResultType.NUMBER) {
                return false;
            }
            if (other.resultType() == this.resultType()) {
                return other.asDouble() == value;
            } else if (other.resultType() == ResultType.INT) {
                return other.asInt() == value;
            } else if (other.resultType() == ResultType.LONG) {
                return other.asLong() == value;
            } else {
                return other.asFloat() == value;
            }
        }
    }

    record StringLit(String value) implements LiteralNode, EvalResult {

        public boolean asBoolean() { return !value.isEmpty(); }

        public int asInt() { return value.length(); }

        public long asLong() { return value.length(); }

        public float asFloat() { return value.length(); }

        public double asDouble() { return value.length(); }

        public String asString() { return value; }

        @Override
        public String toString() { return value; }

        public Object asObject() { return value; }

        public Node asNode() { return this; }

        public List<String> asList() { return List.of(value); }

        public ResultType resultType() { return ResultType.STRING; }

        public String langType() { return "String"; }

        public boolean isRefEqualTo(EvalResult other) {
            if (other.resultType().subType != ResultType.SubResultType.STRING) {
                return false;
            } else {
                return other.asObject() == value;
            }
        }
    }

    record QuoteLit(String value) implements LiteralNode, EvalResult {

        public int asInt() { return value.length(); }

        public long asLong() { return value.length(); }

        public float asFloat() { return value.length(); }

        public double asDouble() { return value.length(); }

        public boolean asBoolean() { return !value.isEmpty(); }

        public String asString() { return value; }

        @Override
        public String toString() { return value; }

        public Object asObject() { return value; }

        public Node asNode() { return this; }

        public List<String> asList() { return List.of(value); }

        public ResultType resultType() { return ResultType.QUOTE; }

        public String langType() { return "Quote"; }

        public boolean isRefEqualTo(EvalResult other) {
            if (other.resultType().subType != ResultType.SubResultType.STRING) {
                return false;
            } else {
                return other.asObject() == value;
            }
        }
    }

    record BooleanLit(boolean value) implements LiteralNode, EvalResult {

        public boolean asBoolean() { return value; }

        public String asString() { return value ? "#t" : "#f"; }

        @Override
        public String toString() { return value ? "#t" : "#f"; }

        public int asInt() { return value ? 1 : 0; }

        public long asLong() { return value ? 1 : 0; }

        public float asFloat() { return value ? 1 : 0; }

        public double asDouble() { return value ? 1 : 0; }

        public Object asObject() { return value; }

        public Node asNode() { return this; }

        public List<Boolean> asList() { return List.of(value); }

        public ResultType resultType() { return ResultType.BOOLEAN; }

        public String langType() { return "boolean"; }

        public boolean isRefEqualTo(EvalResult other) {
            return other.asBoolean() == value(); // FIXME do I really want to do this?
        }
    }

    record ObjectLit(Object value) implements LiteralNode, EvalResult {

        @Override
        public int asInt() { return 1; }

        @Override
        public long asLong() { return 1; }

        @Override
        public float asFloat() { return 1; }

        @Override
        public double asDouble() { return 1; }

        public boolean asBoolean() { return value != null; }

        public String asString() { return toString(); }

        public Object asObject() { return value; }

        public Node asNode() { return this; }

        public List<Object> asList() { return List.of(value); }

        public ResultType resultType() { return ResultType.OBJECT; }

        public String langType() { return value.getClass().getTypeName(); }

        public boolean isRefEqualTo(EvalResult other) {
            return other.asObject() == value;
        }
    }

    record NullLit() implements LiteralNode, EvalResult {

        public int asInt() { return 0; }

        public long asLong() { return 0; }

        public float asFloat() { return 0; }

        public double asDouble() { return 0; }

        public boolean asBoolean() { return false; }

        public String asString() { return "#null"; }

        @Override
        public String toString() { return "#null"; }

        public Object asObject() { return null; }

        public Node asNode() { return this; }

        public ResultType resultType() { return ResultType.NULL; }

        public List<Object> asList() { return List.of(); }

        public String langType() { return "null"; }

        public boolean isRefEqualTo(EvalResult other) {
            return other.asObject() == null;
        }
    }

    record VoidLit() implements LiteralNode, EvalResult {

        public int asInt() { return 0; }

        public long asLong() { return 0; }

        public float asFloat() { return 0; }

        public double asDouble() { return 0; }

        public boolean asBoolean() { return false; }

        public String asString() { return "#void"; }

        @Override
        public String toString() { return ""; }

        public Object asObject() { return this; }

        public Node asNode() { return this; }

        public ResultType resultType() { return ResultType.VOID; }

        public List<Object> asList() { return List.of(); }

        public String langType() { return "void"; }

        public boolean isRefEqualTo(EvalResult other) {
            return other.asObject() == this;
        }
    }

    record ListLit<T>(List<T> value) implements LiteralNode, EvalResult {

        public int asInt() { return value.size(); }

        public long asLong() { return value.size(); }

        public float asFloat() { return value.size(); }

        public double asDouble() { return value.size(); }

        public boolean asBoolean() { return !value.isEmpty(); }

        public String asString() { return value.toString(); }

        public String toString() { return value.toString(); }

        public Object asObject() { return value; }

        public Node asNode() { return this; }

        public List<T> asList() { return value; }

        public ResultType resultType() { return ResultType.LIST; }

        public String langType() { return "List"; }

        public boolean isRefEqualTo(EvalResult other) {
            return other.asObject() == value;
        }
    }

    record LambdaLit(DefinitionNode.LambdaDef value, Environment env) implements LiteralNode, EvalResult {

        public int asInt() { return 1; }

        public long asLong() { return 1; }

        public float asFloat() { return 1; }

        public double asDouble() { return 1; }

        public boolean asBoolean() { return true; }

        public String asString() { return value.toString(); }

        public String toString() { return value.toString(); }

        public Node asNode() { return value; }

        public List<?> asList() { return List.of(value); }

        public Object asObject() { return value; }

        public ResultType resultType() { return ResultType.LAMBDA; }

        public String langType() { return "lambda<" + value.returnType() + ">"; }

        public boolean isRefEqualTo(EvalResult other) {
            return other.asObject() == value; }
    }

}
