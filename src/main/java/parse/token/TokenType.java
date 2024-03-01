package parse.token;

import java.util.Arrays;


public enum TokenType {
    // SINGLE
    LEFT_PAREN("("),
    RIGHT_PAREN(")"),
    LEFT_BRACE("{"),
    RIGHT_BRACE("}"),
    LEFT_BRACKET("["),
    RIGHT_BRACKET("]"),
    COMMA(","),
    DOT("."),
    MINUS("-"),
    PLUS("+"),
    ASTERISK("*"),
    COLON(":"),
    SEMI_COLON(";"),
    SLASH("/"),
    BACK_SLASH("\\"),
    SINGLE_QUOTE("'"),
    QUOTE("\""),
    CARET("^"),
    PERCENT("%"),
    POUND("#"),
    CACHE("$"),
    AMPERSAND("&"),
    EQUAL("="),
    AT("@"),
    GRAVE("`"),
    BAR("|"),
    TILDE("~"),
    BANG("!"),
    GREATER(">"),
    LESS("<"),

    // Compound
    BANG_EQUAL("!="),
    EQUAL_EQUAL("++"),
    GREATER_EQUAL(">="),
    LESS_EQUAL("<="),
    PLUS_PLUS("++"),
    MINUS_MINUS("--"),
    ASSIGN(":="),
    TRUE("#t"),
    FALSE("#f"),

    // Complex
    IDENTIFIER(null),
    STRING(null),
    TYPE(("::")),
    INT(null),
    LONG(null),
    FLOAT(null),
    DOUBLE(null),

    // Keywords
    AND("and"),
    OR("or"),
    XOR("xor"),
    NAND("nand"),
    IF(""),
    ELSE("else"),
    EQUALS("equals"),
    COND("cond"),
    DEFINE("define"),
    FUNC("func"),
    NULL("null"),
    PRINT("print"),
    SUPER("super"),
    THIS("this"),
    FOR_I("for-i"),
    FOR_EACH("for-each"),
    CONS("cons"),
    CAR("car"),
    CAAR("caar"),
    CADR("cadr"),
    CDR("cdr"),
    CDDR("cddr"),
    CDAR("cdar"),
    // End
    EOF(null);

    public final String asString;
    private static final SingleToken[] SINGLE_TOKENS;
    private static final DoubleToken[] DOUBLE_TOKENS;
    private static final KeyWordToken[] KEYWORD_TOKENS;


    static {
        SINGLE_TOKENS = Arrays.stream(TokenType.values())
                .filter(t -> t.asString != null && t.asString.length() == 1)
                .map(t -> new SingleToken(t.asString.charAt(0), t))
                .toArray(SingleToken[]::new);

        DOUBLE_TOKENS = Arrays.stream(TokenType.values())
                .filter(t -> t.asString != null && t.asString.length() == 2)
                .map(t -> new DoubleToken(t.asString.charAt(0), t.asString.charAt(1), t))
                .toArray(DoubleToken[]::new);

        KEYWORD_TOKENS = Arrays.stream(TokenType.values())
                .filter(t -> t.asString != null && t.asString.length() > 2)
                .map(t -> new KeyWordToken(t.asString, t))
                .toArray(KeyWordToken[]::new);
    }

    TokenType(String asString) {
        this.asString = asString;
    }

    public static TokenType matchSingle(char c) {
        for (var t : SINGLE_TOKENS) {
            if (t.chr == c) {
                return t.tokenType;
            }
        }
        return null;
    }

    public static TokenType matchDouble(char c1, char c2) {
        for (var t : DOUBLE_TOKENS) {
            if (t.chr1 == c1 && t.chr2 == c2) {
                return t.tokenType;
            }
        }
        return null;
    }

    public static TokenType matchKeyWord(String word) {
        for (var t : KEYWORD_TOKENS) {
            if (t.keyword.equals(word)) {
                return t.tokenType;
            }
        }
        return null;
    }

    private record SingleToken(char chr, TokenType tokenType) { }


    private record DoubleToken(char chr1, char chr2, TokenType tokenType) { }


    private record KeyWordToken(String keyword, TokenType tokenType) { }
}

