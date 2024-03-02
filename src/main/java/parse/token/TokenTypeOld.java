package parse.token;

import java.util.Arrays;


public enum TokenTypeOld {
    //Lexical
    LEFT_PAREN("(", SubType.LEXICAL),
    RIGHT_PAREN(")", SubType.LEXICAL),
    LEFT_BRACE("{", SubType.LEXICAL),
    RIGHT_BRACE("}", SubType.LEXICAL),
    LEFT_BRACKET("[", SubType.LEXICAL),
    RIGHT_BRACKET("]", SubType.LEXICAL),
    COMMA(",", SubType.LEXICAL),
    SLASH("/", SubType.LEXICAL),
    BACK_SLASH("\\", SubType.LEXICAL),
    SINGLE_QUOTE("'", SubType.LEXICAL),
    QUOTE("\"", SubType.LEXICAL),
    EOF(null, SubType.LEXICAL),

    // Syntactic
    DOT(".", SubType.SYNTACTIC),
    AMPERSAND("&", SubType.SYNTACTIC),
    GRAVE("`", SubType.SYNTACTIC),
    COLON(":", SubType.SYNTACTIC),
    SEMI_COLON(";", SubType.SYNTACTIC),
    TYPE("::", SubType.SYNTACTIC),
    POUND("#", SubType.SYNTACTIC),
    CACHE("$", SubType.SYNTACTIC),
    AT("@", SubType.SYNTACTIC),
    BAR("|", SubType.SYNTACTIC),
    TILDE("~", SubType.SYNTACTIC),
    SUPER("super", SubType.EXPRESSION),
    THIS("this", SubType.EXPRESSION),

    // Operation
    MINUS("-", SubType.OPERATION),
    PLUS("+", SubType.OPERATION),
    ASTERISK("*", SubType.OPERATION),
    CARET("^", SubType.OPERATION),
    PERCENT("%", SubType.OPERATION),
    EQUAL("=", SubType.OPERATION),
    EQUALS("equals", SubType.OPERATION),
    BANG("!", SubType.OPERATION),
    GREATER(">", SubType.OPERATION),
    LESS("<", SubType.OPERATION),
    BANG_EQUAL("!=", SubType.OPERATION),
    EQUAL_EQUAL("++", SubType.OPERATION),
    GREATER_EQUAL(">=", SubType.OPERATION),
    LESS_EQUAL("<=", SubType.OPERATION),
    PLUS_PLUS("++", SubType.OPERATION),
    MINUS_MINUS("--", SubType.OPERATION),
    ASSIGN(":=", SubType.OPERATION),
    AND("and", SubType.OPERATION),
    OR("or", SubType.OPERATION),
    XOR("xor", SubType.OPERATION),
    NAND("nand", SubType.OPERATION),

    // Literals
    TRUE("#t", SubType.LITERAL),
    FALSE("#f", SubType.LITERAL),
    STRING(null, SubType.LITERAL),
    INT(null, SubType.LITERAL),
    LONG(null, SubType.LITERAL),
    FLOAT(null, SubType.LITERAL),
    DOUBLE(null, SubType.LITERAL),
    IDENTIFIER(null, SubType.LITERAL),
    NULL("null", SubType.LITERAL),

    // Expressions

    IF("if", SubType.EXPRESSION),
    ELSE("else", SubType.EXPRESSION),
    COND("cond", SubType.EXPRESSION),
    PRINT("print", SubType.EXPRESSION),
    FOR_I("for-i", SubType.EXPRESSION),
    FOR_EACH("for-each", SubType.EXPRESSION),
    WHILE("while", SubType.EXPRESSION),
    CONS("cons", SubType.EXPRESSION),
    CAR("car", SubType.EXPRESSION),
    CAAR("caar", SubType.EXPRESSION),
    CADR("cadr", SubType.EXPRESSION),
    CDR("cdr", SubType.EXPRESSION),
    CDDR("cddr", SubType.EXPRESSION),
    CDAR("cdar", SubType.EXPRESSION),

    // Definition
    DEFINE("define", SubType.DEFINITION),
    FUNC("func", SubType.DEFINITION);

    public enum SubType {
        EXPRESSION,
        DEFINITION,
        OPERATION,
        LITERAL,
        LEXICAL,
        SYNTACTIC,

    }

    public final String asString;
    public final SubType subType;
    private static final SingleToken[] SINGLE_TOKENS;
    private static final DoubleToken[] DOUBLE_TOKENS;
    private static final KeyWordToken[] KEYWORD_TOKENS;

    static {
        SINGLE_TOKENS = Arrays.stream(TokenTypeOld.values())
                .filter(t -> t.asString != null && t.asString.length() == 1)
                .map(t -> new SingleToken(t.asString.charAt(0), t))
                .toArray(SingleToken[]::new);

        DOUBLE_TOKENS = Arrays.stream(TokenTypeOld.values())
                .filter(t -> t.asString != null && t.asString.length() == 2)
                .map(t -> new DoubleToken(t.asString.charAt(0), t.asString.charAt(1), t))
                .toArray(DoubleToken[]::new);

        KEYWORD_TOKENS = Arrays.stream(TokenTypeOld.values())
                .filter(t -> t.asString != null && t.asString.length() > 2)
                .map(t -> new KeyWordToken(t.asString, t))
                .toArray(KeyWordToken[]::new);
    }

    TokenTypeOld(String asString, SubType subType) {
        this.asString = asString;
        this.subType = subType;
    }

    public static TokenTypeOld matchSingle(char c) {
        for (var t : SINGLE_TOKENS) {
            if (t.chr == c) {
                return t.tokenType;
            }
        }
        return null;
    }

    public static TokenTypeOld matchDouble(char c1, char c2) {
        for (var t : DOUBLE_TOKENS) {
            if (t.chr1 == c1 && t.chr2 == c2) {
                return t.tokenType;
            }
        }
        return null;
    }

    public static TokenTypeOld matchKeyWord(String word) {
        for (var t : KEYWORD_TOKENS) {
            if (t.keyword.equals(word)) {
                return t.tokenType;
            }
        }
        return null;
    }

    private record SingleToken(char chr, TokenTypeOld tokenType) { }

    private record DoubleToken(char chr1, char chr2, TokenTypeOld tokenType) { }

    private record KeyWordToken(String keyword, TokenTypeOld tokenType) { }
}

