package Evaluation;

import parse.node.EvalResult;
import parse.node.LiteralNode;
import parse.node.OperationNode;
import parse.node.ResultType;

import java.util.*;
import java.util.function.Function;

import static parse.node.ResultType.*;


public class OperationVisitor {

    public static Map<Class<? extends OperationNode>, Function<EvalResult[], LiteralNode>> operationMap = new HashMap<>(22);

    static {
        operationMap.put(OperationNode.AddOp.class, OperationVisitor::addOperation);
        operationMap.put(OperationNode.SubtractOp.class, OperationVisitor::subtractOperation);
        operationMap.put(OperationNode.MultiplyOp.class, OperationVisitor::multiplyOperation);
        operationMap.put(OperationNode.DivideOp.class, OperationVisitor::divideOperation);
        operationMap.put(OperationNode.ModuloOp.class, OperationVisitor::moduloOperation);
        operationMap.put(OperationNode.ExponentiateOp.class, OperationVisitor::exponentiateOperation);
        operationMap.put(OperationNode.IncOp.class, OperationVisitor::incrementOperation);
        operationMap.put(OperationNode.DecOp.class, OperationVisitor::decrementOperation);
        operationMap.put(OperationNode.AndOp.class, OperationVisitor::andOperation);
        operationMap.put(OperationNode.OrOp.class, OperationVisitor::orOperation);
        operationMap.put(OperationNode.NorOp.class, OperationVisitor::norOperation);
        operationMap.put(OperationNode.XorOp.class, OperationVisitor::xOrOperation);
        operationMap.put(OperationNode.XNorOp.class, OperationVisitor::xNorOperation);
        operationMap.put(OperationNode.NandOp.class, OperationVisitor::nandOperation);
        operationMap.put(OperationNode.NegateOp.class, OperationVisitor::negate);
        operationMap.put(OperationNode.GreaterThanOp.class, OperationVisitor::greaterThan);
        operationMap.put(OperationNode.GreaterThanEqualOp.class, OperationVisitor::greaterThanEqual);
        operationMap.put(OperationNode.LessThanOp.class, OperationVisitor::lessThan);
        operationMap.put(OperationNode.LessThanEqualOp.class, OperationVisitor::lessThanEqual);
        operationMap.put(OperationNode.EqualityOp.class, OperationVisitor::equals);
        operationMap.put(OperationNode.RefEqualityOp.class, OperationVisitor::refEquality);
        operationMap.put(OperationNode.RefNonEqualityOp.class, OperationVisitor::nonRefEquality);
    }

    // TODO add string concatenation and removal?

    ////////////////
    // Arithmetic //
    ////////////////

    public static LiteralNode addOperation(EvalResult[] operands) {
        ResultType rType = getReturnType(operands);
        if (rType == INT) {
            int result = 0;
            for (int i = 0; i < operands.length; ++i) {
                result += operands[i].asInt();
            }
            return new LiteralNode.IntLit(result);
        }

        if (rType == DOUBLE) {
            double result = 0;
            for (int i = 0; i < operands.length; ++i) {
                result += operands[i].asDouble();
            }
            return new LiteralNode.DoubleLit(result);
        }

        if (rType == LONG) {
            long result = 0;
            for (int i = 0; i < operands.length; ++i) {
                result += operands[i].asLong();
            }
            return new LiteralNode.LongLit(result);
        }

        if (rType == FLOAT) {
            float result = 0;
            for (int i = 0; i < operands.length; ++i) {
                result += operands[i].asFloat();
            }
            return new LiteralNode.FloatLit(result);
        }

        throw new RuntimeException("Non-numerical literal in arithmetic operation");
    }

    public static LiteralNode subtractOperation(EvalResult[] operands) {
        ResultType rType = getReturnType(operands);
        if (rType == INT) {
            int result = 0;
            for (int i = 0; i < operands.length; ++i) {
                if (i == 0) {
                    result = operands[i].asInt();
                    continue;
                }
                result -= operands[i].asInt();
            }
            return new LiteralNode.IntLit(result);
        }

        if (rType == DOUBLE) {
            double result = 0;
            for (int i = 0; i < operands.length; ++i) {
                if (i == 0) {
                    result = operands[i].asDouble();
                    continue;
                }
                result -= operands[i].asDouble();
            }
            return new LiteralNode.DoubleLit(result);
        }

        if (rType == LONG) {
            long result = 0;
            for (int i = 0; i < operands.length; ++i) {
                if (i == 0) {
                    result = operands[i].asLong();
                    continue;
                }
                result -= operands[i].asLong();
            }
            return new LiteralNode.LongLit(result);
        }

        if (rType == FLOAT) {
            float result = 0;
            for (int i = 0; i < operands.length; ++i) {
                if (i == 0) {
                    result = operands[i].asFloat();
                    continue;
                }
                result -= operands[i].asFloat();
            }
            return new LiteralNode.FloatLit(result);
        }

        throw new RuntimeException("Non-numerical literal in arithmetic operation");
    }

    public static LiteralNode multiplyOperation(EvalResult[] operands) {
        ResultType rType = getReturnType(operands);
        System.out.println("ReturnType:" + rType);
        if (rType == INT) {
            int result = 0;
            for (int i = 0; i < operands.length; ++i) {
                if (i == 0) {
                    result = operands[i].asInt();
                    continue;
                }
                result *= operands[i].asInt();
            }
            return new LiteralNode.IntLit(result);
        }

        if (rType == DOUBLE) {
            double result = 0;
            for (int i = 0; i < operands.length; ++i) {
                if (i == 0) {
                    result = operands[i].asDouble();
                    continue;
                }
                result *= operands[i].asDouble();
            }
            return new LiteralNode.DoubleLit(result);
        }

        if (rType == LONG) {
            long result = 0;
            for (int i = 0; i < operands.length; ++i) {
                if (i == 0) {
                    result = operands[i].asLong();
                    continue;
                }
                result *= operands[i].asLong();
            }
            return new LiteralNode.LongLit(result);
        }

        if (rType == FLOAT) {
            float result = 0;
            for (int i = 0; i < operands.length; ++i) {
                if (i == 0) {
                    result = operands[i].asFloat();
                    continue;
                }
                result *= operands[i].asFloat();
            }
            return new LiteralNode.FloatLit(result);
        }

        throw new RuntimeException("Non-numerical literal in arithmetic operation");
    }

    public static LiteralNode divideOperation(EvalResult[] operands) {
        ResultType rType = getReturnType(operands);
        if (rType == INT) {
            int result = 0;
            for (int i = 0; i < operands.length; ++i) {
                if (i == 0) {
                    result = operands[i].asInt();
                    continue;
                }
                result /= operands[i].asInt();
            }
            return new LiteralNode.IntLit(result);
        }

        if (rType == DOUBLE) {
            double result = 0;
            for (int i = 0; i < operands.length; ++i) {
                if (i == 0) {
                    result = operands[i].asDouble();
                    continue;
                }
                result /= operands[i].asDouble();
            }
            return new LiteralNode.DoubleLit(result);
        }

        if (rType == LONG) {
            long result = 0;
            for (int i = 0; i < operands.length; ++i) {
                if (i == 0) {
                    result = operands[i].asLong();
                    continue;
                }
                result /= operands[i].asLong();
            }
            return new LiteralNode.LongLit(result);
        }

        if (rType == FLOAT) {
            float result = 0;
            for (int i = 0; i < operands.length; ++i) {
                if (i == 0) {
                    result = operands[i].asFloat();
                    continue;
                }
                result /= operands[i].asFloat();
            }
            return new LiteralNode.FloatLit(result);
        }

        throw new RuntimeException("Non-numerical literal in arithmetic operation");
    }

    public static LiteralNode moduloOperation(EvalResult[] operands) {
        ResultType rType = getReturnType(operands);
        if (rType == INT) {
            int result = 0;
            for (int i = 0; i < operands.length; ++i) {
                if (i == 0) {
                    result = operands[i].asInt();
                    continue;
                }
                result %= operands[i].asInt();
            }
            return new LiteralNode.IntLit(result);
        }

        if (rType == DOUBLE) {
            double result = 0;
            for (int i = 0; i < operands.length; ++i) {
                System.out.println(operands[i].asDouble());
                if (i == 0) {
                    result = operands[i].asDouble();
                    continue;
                }
                result %= operands[i].asDouble();
            }
            return new LiteralNode.DoubleLit(result);
        }

        if (rType == LONG) {
            long result = 0;
            for (int i = 0; i < operands.length; ++i) {
                if (i == 0) {
                    result = operands[i].asLong();
                    continue;
                }
                result %= operands[i].asLong();
            }
            return new LiteralNode.LongLit(result);
        }

        if (rType == FLOAT) {
            float result = 0;
            for (int i = 0; i < operands.length; ++i) {
                System.out.println(operands[i].asFloat());
                if (i == 0) {
                    result = operands[i].asFloat();
                    continue;
                }
                result %= operands[i].asFloat();
            }
            return new LiteralNode.FloatLit(result);
        }

        throw new RuntimeException("Non-numerical literal in arithmetic operation");
    }

    public static LiteralNode exponentiateOperation(EvalResult[] operands) {
        ResultType rType = getReturnType(operands);
        double result = 0;
        for (int i = 0; i < operands.length; ++i) {
            if (i == 0) {
                result = operands[i].asDouble();
                continue;
            }
            result = Math.pow(result, operands[i].asDouble());
        }
        return new LiteralNode.DoubleLit(result);
    }

    public static LiteralNode incrementOperation(EvalResult[] operands) {
        ResultType rType = getReturnType(operands);
        if (rType == INT) {
            List<Integer> list = new ArrayList<>(operands.length);
            for (int i = 0; i < operands.length; ++i) { list.add(operands[i].asInt() + 1); }
            return new LiteralNode.ListLit<>(list);
        }
        if (rType == DOUBLE) {
            List<Double> list = new ArrayList<>(operands.length);
            for (int i = 0; i < operands.length; ++i) { list.add(operands[i].asDouble() + 1); }
            return new LiteralNode.ListLit<>(list);
        }
        if (rType == LONG) {
            List<Long> list = new ArrayList<>(operands.length);
            for (int i = 0; i < operands.length; ++i) { list.add(operands[i].asLong() + 1); }
            return new LiteralNode.ListLit<>(list);
        }
        if (rType == FLOAT) {
            List<Float> list = new ArrayList<>(operands.length);
            for (int i = 0; i < operands.length; ++i) { list.add(operands[i].asFloat() + 1); }
            return new LiteralNode.ListLit<>(list);
        }
        throw new RuntimeException("Non-numerical literal in arithmetic operation");
    }

    public static LiteralNode decrementOperation(EvalResult[] operands) {
        ResultType rType = getReturnType(operands);
        if (rType == INT) {
            List<Integer> list = new ArrayList<>(operands.length);
            for (int i = 0; i < operands.length; ++i) { list.add(operands[i].asInt() - 1); }
            return list.size() == 1
                   ? new LiteralNode.IntLit(list.getFirst())
                   : new LiteralNode.ListLit<>(Collections.unmodifiableList(list));
        }
        if (rType == DOUBLE) {
            List<Double> list = new ArrayList<>(operands.length);
            for (int i = 0; i < operands.length; ++i) { list.add(operands[i].asDouble() - 1); }
            return list.size() == 1
                   ? new LiteralNode.DoubleLit(list.getFirst())
                   : new LiteralNode.ListLit<>(Collections.unmodifiableList(list));
        }
        if (rType == LONG) {
            List<Long> list = new ArrayList<>(operands.length);
            for (int i = 0; i < operands.length; ++i) { list.add(operands[i].asLong() - 1); }
            return list.size() == 1
                   ? new LiteralNode.LongLit(list.getFirst())
                   : new LiteralNode.ListLit<>(Collections.unmodifiableList(list));
        }
        if (rType == FLOAT) {
            List<Float> list = new ArrayList<>(operands.length);
            for (int i = 0; i < operands.length; ++i) { list.add(operands[i].asFloat() - 1); }
            return list.size() == 1
                   ? new LiteralNode.FloatLit(list.getFirst())
                   : new LiteralNode.ListLit<>(Collections.unmodifiableList(list));
        }
        throw new RuntimeException("Non-numerical literal in arithmetic operation");
    }

    /////////////
    // Boolean //
    /////////////

    public static LiteralNode orOperation(EvalResult[] operands) {
        for (int i = 0; i < operands.length; ++i) {
            var operand = operands[i];
            if (operand.asBoolean()) { return new LiteralNode.BooleanLit(true); }
        }
        return new LiteralNode.BooleanLit(false);
    }

    public static LiteralNode andOperation(EvalResult[] operands) {
        for (int i = 0; i < operands.length; ++i) {
            var operand = operands[i];
            if (!operand.asBoolean()) { return new LiteralNode.BooleanLit(false); }
        }
        return new LiteralNode.BooleanLit(true);
    }

    public static LiteralNode xOrOperation(EvalResult[] operands) {
        int truths = 0;
        for (int i = 0; i < operands.length; ++i) {
            var operand = operands[i];
            if (operand.asBoolean()) { truths++; }
        }
        return new LiteralNode.BooleanLit(truths % 2 == 1);
    }

    public static LiteralNode nandOperation(EvalResult[] operands) {
        for (int i = 0; i < operands.length; ++i) {
            var operand = operands[i];
            if (!operand.asBoolean()) { return new LiteralNode.BooleanLit(true); }
        }
        return new LiteralNode.BooleanLit(false);
    }

    public static LiteralNode norOperation(EvalResult[] operands) {
        for (int i = 0; i < operands.length; ++i) {
            var operand = operands[i];
            if (operand.asBoolean()) { return new LiteralNode.BooleanLit(false); }
        }
        return new LiteralNode.BooleanLit(true);
    }

    public static LiteralNode xNorOperation(EvalResult[] operands) {
        int truths = 0;
        for (int i = 0; i < operands.length; ++i) {
            var operand = operands[i];
            if (operand.asBoolean()) { truths++; }
        }
        return new LiteralNode.BooleanLit(truths % 2 == 0);
    }

    public static LiteralNode negate(EvalResult[] operands) {
        return operands[0].asBoolean()
               ? new LiteralNode.BooleanLit(false)
               : new LiteralNode.BooleanLit(true);

    }

    ////////////////
    // Comparison //
    ////////////////

    public static LiteralNode greaterThan(EvalResult[] operands) {
        ResultType rType = getReturnType(operands);
        switch (rType) {
            case DOUBLE -> {
                for (int i = 1; i < operands.length; ++i) {
                    if (!(operands[i - 1].asDouble() > operands[i].asDouble())) {
                        return new LiteralNode.BooleanLit(false);
                    }
                }
            }

            case LONG -> {
                for (int i = 1; i < operands.length; ++i) {
                    if (!(operands[i - 1].asLong() > operands[i].asLong())) {
                        return new LiteralNode.BooleanLit(false);
                    }
                }
            }
            case FLOAT -> {
                for (int i = 1; i < operands.length; ++i) {
                    if (!(operands[i - 1].asFloat() > operands[i].asFloat())) {
                        return new LiteralNode.BooleanLit(false);
                    }
                }
            }
            default -> {
                for (int i = 1; i < operands.length; ++i) {
                    if (!(operands[i - 1].asInt() > operands[i].asInt())) {
                        return new LiteralNode.BooleanLit(false);
                    }
                }
            }
        }
        return new LiteralNode.BooleanLit(true);
    }

    public static LiteralNode greaterThanEqual(EvalResult[] operands) {
        ResultType rType = getReturnType(operands);
        switch (rType) {
            case DOUBLE -> {
                for (int i = 1; i < operands.length; ++i) {
                    if (!(operands[i - 1].asDouble() >= operands[i].asDouble())) {
                        return new LiteralNode.BooleanLit(false);
                    }
                }
            }

            case LONG -> {
                for (int i = 1; i < operands.length; ++i) {
                    if (!(operands[i - 1].asLong() >= operands[i].asLong())) {
                        return new LiteralNode.BooleanLit(false);
                    }
                }
            }
            case FLOAT -> {
                for (int i = 1; i < operands.length; ++i) {
                    if (!(operands[i - 1].asFloat() >= operands[i].asFloat())) {
                        return new LiteralNode.BooleanLit(false);
                    }
                }
            }
            default -> {
                for (int i = 1; i < operands.length; ++i) {
                    if (!(operands[i - 1].asInt() >= operands[i].asInt())) {
                        return new LiteralNode.BooleanLit(false);
                    }
                }
            }
        }
        return new LiteralNode.BooleanLit(true);
    }

    public static LiteralNode lessThan(EvalResult[] operands) {
        ResultType rType = getReturnType(operands);
        switch (rType) {
            case DOUBLE -> {
                for (int i = 1; i < operands.length; ++i) {
                    if (!(operands[i - 1].asDouble() < operands[i].asDouble())) {
                        return new LiteralNode.BooleanLit(false);
                    }
                }
            }

            case LONG -> {
                for (int i = 1; i < operands.length; ++i) {
                    if (!(operands[i - 1].asLong() < operands[i].asLong())) {
                        return new LiteralNode.BooleanLit(false);
                    }
                }
            }
            case FLOAT -> {
                for (int i = 1; i < operands.length; ++i) {
                    if (!(operands[i - 1].asFloat() < operands[i].asFloat())) {
                        return new LiteralNode.BooleanLit(false);
                    }
                }
            }
            default -> {
                for (int i = 1; i < operands.length; ++i) {
                    if (!(operands[i - 1].asInt() < operands[i].asInt())) {
                        return new LiteralNode.BooleanLit(false);
                    }
                }
            }
        }
        return new LiteralNode.BooleanLit(true);
    }

    public static LiteralNode lessThanEqual(EvalResult[] operands) {
        ResultType rType = getReturnType(operands);
        switch (rType) {
            case DOUBLE -> {
                for (int i = 1; i < operands.length; ++i) {
                    if (!(operands[i - 1].asDouble() <= operands[i].asDouble())) {
                        return new LiteralNode.BooleanLit(false);
                    }
                }
            }

            case LONG -> {
                for (int i = 1; i < operands.length; ++i) {
                    if (!(operands[i - 1].asLong() <= operands[i].asLong())) {
                        return new LiteralNode.BooleanLit(false);
                    }
                }
            }
            case FLOAT -> {
                for (int i = 1; i < operands.length; ++i) {
                    if (!(operands[i - 1].asFloat() <= operands[i].asFloat())) {
                        return new LiteralNode.BooleanLit(false);
                    }
                }
            }
            default -> {
                for (int i = 1; i < operands.length; ++i) {
                    if (!(operands[i - 1].asInt() <= operands[i].asInt())) {
                        return new LiteralNode.BooleanLit(false);
                    }
                }
            }
        }
        return new LiteralNode.BooleanLit(true);
    }

    public static LiteralNode equals(EvalResult[] operands) {
        for (int i = 1; i < operands.length; ++i) {
            if (operands[i - 1].asObject() == null && operands[i].asObject() != null) {
                return new LiteralNode.BooleanLit(false);
            } else if (!operands[i - 1].asObject().equals(operands[i].asObject())) {
                return new LiteralNode.BooleanLit(false);
            }
        }
        return new LiteralNode.BooleanLit(true);
    }

    public static LiteralNode refEquality(EvalResult[] operands) {
        for (int i = 1; i < operands.length; ++i) {
            if (!operands[i - 1].isRefEqualTo(operands[i])) {
                return new LiteralNode.BooleanLit(false);
            }
        }
        return new LiteralNode.BooleanLit(true);
    }

    public static LiteralNode nonRefEquality(EvalResult[] operands) {
        for (int i = 0; i < operands.length - 1; i++) {
            for (int j = i + 1; j < operands.length; j++) {
                if (operands[i].isRefEqualTo(operands[j])) {
                    return new LiteralNode.BooleanLit(false);
                }
            }
        }
        return new LiteralNode.BooleanLit(true);
    }

//    public static LiteralNode equalityCheck(EvalResult[] operands) {
//        ResultType type = getReturnType(operands);
//        switch (type) {
//            case INT -> {
//                for (int i = 1; i < operands.length; ++i) {
//                    var prev = operands[i - 1];
//                    var next = operands[i];
//                    if (prev.resultType() != next.resultType() || prev.asInt() != next.asInt()) {
//                        return new LiteralNode.BooleanLit(false);
//                    }
//                }
//            }
//            case DOUBLE -> {
//                for (int i = 1; i < operands.length; ++i) {
//                    var prev = operands[i - 1];
//                    var next = operands[i];
//                    if (prev.resultType() != next.resultType() || prev.asDouble() != next.asDouble()) {
//                        return new LiteralNode.BooleanLit(false);
//                    }
//                }
//            }
//            case LONG -> {
//                for (int i = 1; i < operands.length; ++i) {
//                    var prev = operands[i - 1];
//                    var next = operands[i];
//                    if (prev.resultType() != next.resultType() || prev.asLong() != next.asLong()) {
//                        return new LiteralNode.BooleanLit(false);
//                    }
//                }
//            }
//            case FLOAT -> {
//                for (int i = 1; i < operands.length; ++i) {
//                    var prev = operands[i - 1];
//                    var next = operands[i];
//                    if (prev.resultType() != next.resultType() || prev.asFloat() != next.asFloat()) {
//                        return new LiteralNode.BooleanLit(false);
//                    }
//                }
//            }
//            case BOOLEAN -> {
//                for (int i = 1; i < operands.length; ++i) {
//                    var prev = operands[i - 1];
//                    var next = operands[i];
//                    if (prev.resultType() != next.resultType() || prev.asBoolean() != next.asBoolean()) {
//                        return new LiteralNode.BooleanLit(false);
//                    }
//                }
//            }
//            case STRING, OBJECT, QUOTE, NULL, LIST, NODE -> {
//                for (int i = 1; i < operands.length; ++i) {
//                    var prev = operands[i - 1];
//                    var next = operands[i];
//                    if (prev.resultType() != next.resultType() || prev.asObject() != next.asObject()) {
//                        return new LiteralNode.BooleanLit(false);
//                    }
//                }
//
//            }
//        }
//        return new LiteralNode.BooleanLit(true);
//    }

//    public static LiteralNode nonEqualityCheck(EvalResult[] operands) {
//        for (int i = 1; i < operands.length; ++i) {
//            var prev = operands[i - 1];
//            var next = operands[i];
//            if (prev.resultType() == INT) {
//                var val = prev.asInt();
//                if (next.resultType() == INT && val == next.asInt()) {
//                    return new LiteralNode.BooleanLit(false);
//                } else if (next.resultType() == DOUBLE && val == next.asDouble()) {
//                    return new LiteralNode.BooleanLit(false);
//                } else if (next.resultType() == LONG && val == next.asLong()) {
//                    return new LiteralNode.BooleanLit(false);
//                } else if (next.resultType() == FLOAT && val == next.asFloat()) {
//                    return new LiteralNode.BooleanLit(false);
//                }
//            } else if (prev.resultType() == DOUBLE) {
//                var val = prev.asDouble();
//                if (next.resultType() == INT && val == next.asInt()) {
//                    return new LiteralNode.BooleanLit(false);
//                } else if (next.resultType() == DOUBLE && val == next.asDouble()) {
//                    return new LiteralNode.BooleanLit(false);
//                } else if (next.resultType() == LONG && val == next.asLong()) {
//                    return new LiteralNode.BooleanLit(false);
//                } else if (next.resultType() == FLOAT && val == next.asFloat()) {
//                    return new LiteralNode.BooleanLit(false);
//                }
//            }else if (prev.resultType() == LONG) {
//                var val = prev.asDouble();
//                if (next.resultType() == INT && val == next.asInt()) {
//                    return new LiteralNode.BooleanLit(false);
//                } else if (next.resultType() == DOUBLE && val == next.asDouble()) {
//                    return new LiteralNode.BooleanLit(false);
//                } else if (next.resultType() == LONG && val == next.asLong()) {
//                    return new LiteralNode.BooleanLit(false);
//                } else if (next.resultType() == FLOAT && val == next.asFloat()) {
//                    return new LiteralNode.BooleanLit(false);
//                }
//            }else if (prev.resultType() == FLOAT) {
//                var val = prev.asFloat();
//                if (next.resultType() == INT && val == next.asInt()) {
//                    return new LiteralNode.BooleanLit(false);
//                } else if (next.resultType() == DOUBLE && val == next.asDouble()) {
//                    return new LiteralNode.BooleanLit(false);
//                } else if (next.resultType() == LONG && val == next.asLong()) {
//                    return new LiteralNode.BooleanLit(false);
//                } else if (next.resultType() == FLOAT && val == next.asFloat()) {
//                    return new LiteralNode.BooleanLit(false);
//                }
//            } else if (prev.resultType() == BOOLEAN) {
//                if (next.resultType() == BOOLEAN) {
//                    if (prev.asBoolean() == next.asBoolean()) {
//                        return new LiteralNode.BooleanLit(false);
//                    }
//                }
//            } else {
//                if (prev.asObject() == next.asObject()) {
//                    return new LiteralNode.BooleanLit(false);
//                }
//            }
//
//        }
//        return new LiteralNode.BooleanLit(true);
//    }

    /////////////
    // Helpers //
    /////////////
    public static ResultType getReturnType(EvalResult[] operands) {
        ResultType resultType = null;
        for (int i = 0; i < operands.length; ++i) {
            var op = operands[i];
            var opR = op.resultType();
//            if (opR.subType != SubResultType.NUMBER) { //FIXME allow non numeric inputs to operators?
//                throw new IllegalStateException("Non-numerical expression or literal");
//            }
            if (resultType == null) {
                resultType = opR;
                continue;
            }
            // return double, as its top precision if doubles involved
            if (opR == DOUBLE || resultType == DOUBLE) { return DOUBLE; }

            if (opR == LONG) {
                if (resultType != FLOAT) {
                    resultType = LONG;
                } else {
                    return DOUBLE; // Can just use doubles since we have floating point and longs involved;
                }
                continue;
            }

            if (opR == INT) {
                if (resultType == LONG) { continue; }
                if (resultType == FLOAT) { return DOUBLE; }
                resultType = INT;
            }
        }
        return resultType;
    }

}
