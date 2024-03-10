package parse;

import language.types.data.Pair;
import parse.node.*;
import parse.token.Token;
import parse.token.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public class Parser {
    private List<Token> tokens;
    private int current = 0;
    private int depth = 0;

    public Parser() {

    }

    private final Function<String, IllegalStateException> onError = (String msg) -> {
        Token currToken = peek();
        String error = String.format("[Line: %d, Token: %d] Error: %s", currToken.line(), current, msg);
        printRemainingTokens();
        return new IllegalStateException(error);
    };

    public Node.Program process(List<Token> tokens) {
        this.tokens = tokens;
        current = 0;
        depth = 0;

        List<Node> topLevelExpressions = new ArrayList<>(100);

        while (haveNext()) {
            topLevelExpressions.add(parseExpressionData());
        }
        return new Node.Program(topLevelExpressions);
    }

    private Node parseSExpr() {
        Node expression = null;
        consumeLParen("Expected start of s-expression: " + peek().type());
        expression = parseExpressionData();
        if (peek().type() == TokenType.Syntactic.COLON) {
            expression = parseObjectCall(expression);
        }
        consumeRParen("Expected closing parenthesis of expression, found: " + peek().type());

        if (expression == null) {
            return LiteralNode.NIL_LIST;
        }
        return expression;
    }

    private Node parseObjectCall(Node objectExpr) {
        consume(TokenType.Syntactic.COLON, "Expected colon, found: " + peek().type()); // Consume colon
        boolean isField = false;
        if (peekN(2).type() == TokenType.Syntactic.DOT) {
            isField = true;
            advance(); // Consume dot
        }
        Token idToken = consume(TokenType.Literal.IDENTIFIER, "Expected Identifier, Found: " + peek().type());
        String identifier = idToken.literal().toString();

        String name = null;
        List<ExpressionNode.Accessor> accessors = null;

        if (identifier.contains(":")) {
            var splitId = parseAccessors(identifier);
            name = splitId.car();
            accessors = splitId.cdr();
        } else {
            name = identifier;
        }

        List<ExpressionNode.FuncArg> args = new ArrayList<>(5);

        while (peek().type() != TokenType.Lexical.RIGHT_PAREN) {
            ExpressionNode.FuncArg funcArg = parseFuncArgument();
            args.add(funcArg);
        }

        ExpressionNode.FunctionCall funcCall = new ExpressionNode.FunctionCall(name, accessors, args);
        return new ExpressionNode.OnObjectCall(objectExpr, funcCall, isField);
    }

    private Node parseFunc() {
        consume(TokenType.Definition.FUNC, "Expected func, found: " + peek().type());
        String name = consume(TokenType.Literal.IDENTIFIER, "Func definition without name").lexeme();
        List<TokenType.Modifier> modifiers = match(TokenType.Modifier.values()) ? parseModifiers() : null;
        List<DefinitionNode.ParamDef> parameters = parseParameters();

        // Consume opening and closing parens for implicit multi expressions
        consumeLParen("");
        Node body = parseMultiExpr();
        consumeRParen("");

        String returnType = (peek().type() == TokenType.Syntactic.TYPE) ? advance().lexeme() : null;
        return new DefinitionNode.FunctionDef(name, modifiers, parameters, body, returnType);
    }

    private Node parseDefine() {
        consume(TokenType.Definition.DEFINE, "Expected define, found: " + peek().type());

        String name = consume(TokenType.Literal.IDENTIFIER, "Definition without name").lexeme();
        List<TokenType.Modifier> modifiers = match(TokenType.Modifier.values()) ? parseModifiers() : null;
        String varType = match(TokenType.Syntactic.TYPE) ? advance().literal().toString() : null;

        DefinitionNode definitionNode = null;

        if (peek().type() == TokenType.Lexical.LEFT_PAREN) {
            if (peekN(2).type() == TokenType.Definition.LAMBDA) {
                consumeLParen("Expected start of s-expr, found " + peek().type());
                DefinitionNode.LambdaDef lambda = parseLambda();
                definitionNode = new DefinitionNode.FunctionDef(name, lambda);
                consumeRParen("Expected end of s-expr, found: " + peek().type());

            } else {
                definitionNode = new DefinitionNode.VariableDef(name, modifiers, varType, parseExpressionData());
            }
        } else if (match(TokenType.Literal.values())) {
            definitionNode = new DefinitionNode.VariableDef(name, modifiers, varType, parseLiteral());
        } else if (peek().type() == TokenType.Syntactic.GRAVE) {
            definitionNode = new DefinitionNode.VariableDef(name, modifiers, varType, parseQuote());
        }
        if (definitionNode == null) {
            throw onError.apply("Invalid syntax in define: " + peek().type());
        }

        if (definitionNode instanceof DefinitionNode.FunctionDef && varType != null) { // TODO implement actual warnings
            System.out.printf("Type specifier %s, is unused. Variable type specification ignored on lambda bound variables.%n", varType);
        }

        return definitionNode;
    }

    private DefinitionNode.LambdaDef parseLambda() {
        consume(TokenType.Definition.LAMBDA, "Expected lambda, found:" + peek().type());
        List<TokenType.Modifier> modifiers = match(TokenType.Modifier.values()) ? parseModifiers() : null;

        List<DefinitionNode.ParamDef> parameters = parseParameters();

        Node body = null;
        // Consume opening and closing parens for implicit multi statements
        if (peek().type() == TokenType.Lexical.LEFT_PAREN) {
            consumeLParen("");
            body = parseMultiExpr();
            consumeRParen("");
        } else {
            body = parseLiteral();
        }

        String returnType = (peek().type() == TokenType.Syntactic.TYPE) ? advance().lexeme() : null;

        return new DefinitionNode.LambdaDef(modifiers, parameters, body, returnType);
    }

    private List<DefinitionNode.ParamDef> parseParameters() {

        consumeLParen("Expected opening parenthesis for parameters");
        List<DefinitionNode.ParamDef> params = new ArrayList<>(3);

        boolean optional = false;
        while (peek().type() != TokenType.Lexical.RIGHT_PAREN && haveNext()) {
            List<TokenType.Modifier> modifiers = parseModifiers();
            if (!optional) {
                optional = modifiers.contains(TokenType.Modifier.OPTIONAL);
            }
            boolean dynamic = modifiers.contains(TokenType.Modifier.DYNAMIC);
            boolean mutable = modifiers.contains(TokenType.Modifier.MUTABLE);

            String name = consume(TokenType.Literal.IDENTIFIER, "Parameter Identifier expected").literal().toString();
            Node value = null;
            String type = null;

            if (peek().type() == TokenType.Syntactic.EQUAL) {
                if (!optional) {
                    throw onError.apply("Must specify &opt to declare optional parameters");
                }
                advance(); // skip = token it is just syntactical
                value = parseLiteral();
            }

            if (peek().type() == TokenType.Syntactic.TYPE) {
                type = advance().literal().toString();
            }
            params.add(new DefinitionNode.ParamDef(name, type, optional, value, dynamic, mutable || dynamic));
        }

        consumeRParen("Error parsing parameters");
        return params;
    }

    private Node parseExpressionData() {
        Token token = peek();
        // Recurse nested expressions
        if (token.type() == TokenType.Lexical.LEFT_PAREN) {
            return parseSExpr();
        }
        return switch (token.type()) {
            // TODO add case for inline arrays/lists
            case TokenType.Definition definition -> switch (definition) {
                case DEFINE -> parseDefine();
                case FUNC -> parseFunc();
                case LAMBDA -> parseLambda();
            };
            case TokenType.Expression expression -> parseExactExpression(expression);
            case TokenType.Operation operation -> parseOperation(operation);
            case TokenType.Literal __ -> parseLiteral();
            case TokenType.Syntactic.GRAVE -> parseQuote();
            case TokenType.Lexical lexical
                    when lexical == TokenType.Lexical.RIGHT_PAREN
                    && previous().type() == TokenType.Lexical.LEFT_PAREN -> LiteralNode.NIL_LIST;
            default -> throw onError.apply("Unexpected syntax in expression: " + peek().type());

        };

    }

    private Node parseExactExpression(TokenType.Expression expression) {
        consume(expression, "Expected expression, found:" + peek().lexeme());
        return switch (expression) {
            case ASSIGN -> parseAssign();
            case IF -> parseIf();
            case COND -> parseCond();
            case BEGIN -> parseMultiExpr();
            case PRINT -> parsePrint();
            case LIST -> parsePairList();
            case LACC -> parseListAccess();
//            case FOR_I -> { }
//            case FOR_EACH -> { }
            case WHILE -> parseWhile();
            case CONS -> parseCons();
            case CAR -> ExpressionNode.ListAccess.ofPattern("f", parsePair());
            case CDR -> ExpressionNode.ListAccess.ofPattern("r", parsePair());

            default -> throw onError.apply("Unsupported operation: " + peek().lexeme());
        };
    }

    private Node parseListAccess() {
        if (peek().type() == TokenType.Syntactic.GRAVE) {
            advance(); // Consume grave
            String pattern = advance().literal().toString();
            Node list = parsePair();
            return ExpressionNode.ListAccess.ofPattern(pattern, list);
        } else {
            Node indexExpr = parseExpressionData();
            Node list = parsePair();
            return ExpressionNode.ListAccess.ofIndex(indexExpr, list);
        }
    }

    private Node parsePairList() {
        List<Node> elements = new ArrayList<>(10);
        while (peek().type() != TokenType.Lexical.RIGHT_PAREN) {
            elements.add(parseExpressionData());
        }
        if (elements.isEmpty()) { return LiteralNode.NIL_LIST; }
        return new ExpressionNode.PairListExpression(elements);
    }

    private Node parsePair() {
        Node pair = parseExpressionData();
        if (peek().type() != TokenType.Lexical.RIGHT_PAREN) {
            throw onError.apply("Invalid argument count");
        }
        return pair;
    }

    private Node parseCons() {
        Node car = parseExpressionData();
        Node cdr = parseExpressionData();

        if (peek().type() != TokenType.Lexical.RIGHT_PAREN) {
            throw onError.apply("Cons must only have 2 arguments");
        }
        return new ExpressionNode.ConsExpr(car, cdr);
    }

    private Node parseMultiExpr() {
        List<Node> expressions = new ArrayList<>(4);

        while (peek().type() != TokenType.Lexical.RIGHT_PAREN) {
            expressions.add(parseExpressionData());
        }
        if (expressions.isEmpty()) {
            throw onError.apply("Expected one or more expressions");
        }
        if (expressions.size() > 1) {
            return new ExpressionNode.MultiExpr(expressions);
        } else {
            return expressions.getFirst();
        }
    }

    private ExpressionNode parseAssign() {

        String identifier = consume(TokenType.Literal.IDENTIFIER, "Expected identifier for assignment").lexeme();
        Node value = parseExpressionData();
        return new ExpressionNode.AssignOp(identifier, value);
    }

    private ExpressionNode parseIf() {
        Node condition = parseExpressionData();
        Node thenBranch = parseExpressionData();
        ExpressionNode.CondBranch condBranch = new ExpressionNode.CondBranch(condition, thenBranch);
        Node elseBranch = peek().type() != TokenType.Lexical.RIGHT_PAREN ? parseExpressionData() : null;
        return new ExpressionNode.IfExpr(condBranch, elseBranch);
    }

    private ExpressionNode parsePrint() {
        if (peek().literal() instanceof LiteralNode literal) {
            return new ExpressionNode.PrintExpr(literal);
        } else {
            return new ExpressionNode.PrintExpr(parseExpressionData());
        }
    }

    private ExpressionNode.CondBranch parseCondBranch() {
        consumeLParen("Expected opening parenthesis for conditional branch expression");
        Node condition = parseExpressionData();
        Node thenBranch = parseExpressionData();
        consumeRParen("Expected closing parenthesis for conditional branch expression");
        return new ExpressionNode.CondBranch(condition, thenBranch);
    }

    private ExpressionNode parseCond() {
        List<ExpressionNode.CondBranch> condBranches = new ArrayList<>(5);
        Node elseBranch = null;

        while (peek().type() != TokenType.Lexical.RIGHT_PAREN) {
            printRemainingTokens();
            if (peekN(2).type() == TokenType.Syntactic.ELSE) {
                consumeLParen("Expected opening parenthesis for else");
                consume(TokenType.Syntactic.ELSE, "Expected else symbol");
                elseBranch = parseExpressionData();
                consumeRParen("Expecting closing parenthesis for else");
                break;
            }
            condBranches.add(parseCondBranch());
        }
        if (condBranches.isEmpty()) {
            throw onError.apply("Cond expression must have at least one branch");
        }
        return new ExpressionNode.CondExpr(condBranches, elseBranch);
    }

    private OperationNode parseOperation(TokenType operation) {
        consume(operation, "Expected operation, found:" + peek().type());

        List<Node> operands = new ArrayList<>(5);
        while (peek().type() != TokenType.Lexical.RIGHT_PAREN && haveNext()) {
            operands.add(parseExpressionData());
        }

        if (operation == TokenType.Operation.NEGATE && operands.size() > 1) {
            throw onError.apply("Negate is only valid as a unary operation");
        }
//        if (operands.size() < 2 && (operation != TokenType.Operation.)) {
//            throw onError.apply("Operation requires 2 or more arguments");
//        }

        return OperationNode.getOperationNode((TokenType.Operation) operation, operands);

    }

    private Node parseQuote() {
        advance(); // Consume grave
        Node node = parseExpressionData();
        return new LiteralNode.QuoteLit(node);
    }

    private Node parseLiteral() {
        Token token = advance();
        if (token.type() instanceof TokenType.Literal literal) {
            return switch (literal) {
                case TRUE -> new LiteralNode.BooleanLit(true);
                case FALSE -> new LiteralNode.BooleanLit(false);
                case STRING -> new LiteralNode.StringLit(token.literal().toString());
                case INT -> new LiteralNode.IntLit((Integer) token.literal());
                case LONG -> new LiteralNode.LongLit((Long) token.literal());
                case FLOAT -> new LiteralNode.FloatLit((Float) token.literal());
                case DOUBLE -> new LiteralNode.DoubleLit((Double) token.literal());
                case IDENTIFIER -> parseIdentifier(token.literal().toString());
                case JAVA_IDENTIFIER -> parseJavaIdentifier(token.literal().toString());
                case NULL -> new LiteralNode.NullLit();
            };
        }
        throw (onError.apply("Expected literal value"));
    }

    private Pair<String, List<ExpressionNode.Accessor>> parseAccessors(String identifier) {
        String name = null;
        List<ExpressionNode.Accessor> accessors = new ArrayList<>(4);

        char[] idAsChars = identifier.toCharArray();
        int currIdx = 0;
        boolean isField = false;
        for (int i = 0; i < idAsChars.length; ++i) {
            if (idAsChars[i] == ':') {
                if (name == null) {
                    name = identifier.substring(0, i);
                } else {
                    accessors.add(new ExpressionNode.Accessor(isField, identifier.substring(currIdx, i)));
                    isField = false;
                }

                if (idAsChars.length + 1 > idAsChars.length && idAsChars[i + 1] == '.') {
                    currIdx = i + 2;
                    isField = true;
                } else {
                    currIdx = i + 1;
                }
            }
        }
        accessors.add(new ExpressionNode.Accessor(isField, identifier.substring(currIdx)));
        return Pair.of(name, accessors);
    }

    private Node parseJavaIdentifier(String identifier) {
        String name = null;
        List<ExpressionNode.Accessor> accessors = null;

        if (identifier.contains(":")) {
            var splitId = parseAccessors(identifier);
            name = splitId.car();
            accessors = splitId.cdr();
        } else {
            name = identifier;
        }

        if (previousN(2).type() == TokenType.Lexical.LEFT_PAREN) {
            List<ExpressionNode.FuncArg> args = new ArrayList<>(5);
            while (peek().type() != TokenType.Lexical.RIGHT_PAREN) {
                args.add(parseFuncArgument());
            }
            return new ExpressionNode.JavaFuncCall(name, accessors, args);
        } else {
            throw onError.apply("@<java> calls must occur inside an expression");
        }
    }

    private Node parseIdentifier(String identifier) {
        String name = null;
        List<ExpressionNode.Accessor> accessors = null;

        if (identifier.contains(":")) {
            var splitId = parseAccessors(identifier);
            name = splitId.car();
            accessors = splitId.cdr();
        } else {
            name = identifier;
        }

        if (previousN(2).type() == TokenType.Lexical.LEFT_PAREN) {
            List<ExpressionNode.FuncArg> args = new ArrayList<>(5);
            boolean atOpt = false;
            while (peek().type() != TokenType.Lexical.RIGHT_PAREN) {
                ExpressionNode.FuncArg funcArg = parseFuncArgument();
                if (funcArg.isNamed()) { atOpt = true; }
                if (atOpt && !funcArg.isNamed()) {
                    throw onError.apply("All arguments following first named argument must be also named");
                }
                args.add(funcArg);
            }
            return new ExpressionNode.FunctionCall(name, accessors, args);
        } else {
            return new ExpressionNode.LiteralCall(name);
        }
    }

    private ExpressionNode.FuncArg parseFuncArgument() {
        if (peek().type() == TokenType.Syntactic.COLON) {
            advance(); // consume colon
            String name = consume(TokenType.Literal.IDENTIFIER, "Expected named identifier for argument").lexeme();
            Node arg = parseExpressionData();
            return new ExpressionNode.FuncArg(arg, name);
        } else {
            Node arg = parseExpressionData();
            return new ExpressionNode.FuncArg(arg, null);
        }
    }

    private List<TokenType.Modifier> parseModifiers() {
        List<TokenType.Modifier> modifiers = new ArrayList<>(3);
        while (match(TokenType.Modifier.values())) {
            modifiers.add((TokenType.Modifier) advance().type());
        }
        return modifiers;
    }

    private ExpressionNode parseWhile() {
        //TODO add some checks?
        boolean doWhile = false;
        if (peek().type() == TokenType.Modifier.DO) {
            doWhile = true;
            advance();
        }
        Node condition = parseExpressionData();
        Node expression = parseMultiExpr();
        return new ExpressionNode.WhileLoopExpr(condition, expression, doWhile);
    }

    private Token advance() {
        // debug
        if (peek().type() == TokenType.Lexical.LEFT_PAREN || peek().type() == TokenType.Lexical.RIGHT_PAREN) {
            printRemainingTokens();
            throw new RuntimeException("Parenthesis should only be advanced via consumeParen");
        }
        //

        if (haveNext()) { current++; }
        return previous();
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String error) {
        // DEBUG
        if (peek().type() == TokenType.Lexical.LEFT_PAREN || peek().type() == TokenType.Lexical.RIGHT_PAREN) {
            printRemainingTokens();
            throw new RuntimeException("Parenthesis should only be advanced via consumeParen");
        }
        //

        if (check(type)) {
            return advance();
        }
        throw (onError.apply(error));
    }

    private int consumeLParen(String error) {
        if (peek().type() == TokenType.Lexical.LEFT_PAREN) {
            if (haveNext()) { current++; }
            return ++depth;
        }
        throw (onError.apply(error));
    }

    private int consumeRParen(String error) {
        if (peek().type() == TokenType.Lexical.RIGHT_PAREN) {
            if (haveNext()) { current++; }
            return --depth;
        }
        throw (onError.apply(error));
    }

    private boolean check(TokenType type) {
        if (!haveNext()) { return false; }
        return peek().type() == type;
    }

    private boolean containsModifier(List<TokenType.Modifier> modList, TokenType.Modifier... mods) {
        for (int i = 0; i < mods.length; ++i) {
            if (modList.contains(mods[i])) {
                return true;
            }
        }
        return false;
    }

    private boolean haveNext() {
        return peek().type() != TokenType.Lexical.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token peekN(int n) {
        return tokens.get(current + (n - 1));
    }

    private Token previous() {
        return tokens.get(Math.max(0, current - 1));
    }

    private Token previousN(int n) {
        return tokens.get(Math.max(0, current - n));
    }

    private void printRemainingTokens() {
        System.out.println("Remaining tokens: ");
        for (int i = current; i < tokens.size(); ++i) {
            System.out.print(tokens.get(i).type() + ", ");
        }
        System.out.println();
    }
}
