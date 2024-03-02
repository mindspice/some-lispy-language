package parse;

import parse.token.Token;
import parse.token.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;


public class LexerOld {

    private final String source;
    private final List<Token> tokens;
    private int startIndex = 0;
    private int currIndex = 0;
    private int lineNum = 1;

    public LexerOld(String source) {
        this.source = source;
        this.tokens = new ArrayList<>(source.length() / 5);
    }

    public List<Token> start() {
        while (haveNext()) {
            startIndex = currIndex;
            char currChar = advance();

            switch (currChar) {
                case ' ', '\r', '\t' -> { continue; }
                case '\n' -> {
                    lineNum++;
                    continue;
                }
            }

            if (lexDualToken(currChar)) { continue; }
            if (lexSingleToken(currChar)) { continue; }

            if (isNumeric(currChar)) {
                lexNumber();
                continue;
            }

            if (lexKeywordOrIdentifier()) { continue; }

            //System.out.println(currChar);
            throw new IllegalStateException("Invalid formatting encountered on line: " + lineNum);
        }
        addToken(TokenType.EOF);
        return tokens;
    }

    private boolean lexSingleToken(char c) {
        TokenType singleToken = TokenType.matchSingle(c);
        if (singleToken == null) { return false; }

        switch (singleToken) { //TODO add comments
            case TokenType.QUOTE -> {
                lexString();
                return true;
            }
            default -> addToken(singleToken);
        }
        return true;
    }

    private boolean lexDualToken(char c1) {
        if (!haveNext() || peekOne() == ' ') {
            return false;
        }
        TokenType dblToken = TokenType.matchDouble(c1, peekOne());
        if (dblToken == null) { return false; }
        advance(); // consume 2nd char

        switch (dblToken) {
            case TokenType.TYPE -> lexType();
            default -> addToken(dblToken);
        }
        return true;
    }

    private boolean lexType() {
        while (!isDefEnd(peekOne()) && haveNext()) {
            advance();
        }
        addToken(TokenType.TYPE, source.substring(startIndex + 2, currIndex));
        return true;
    }

    // TODO handled nested strings

    private void lexString() {
        char peeked;
        while ((peeked = peekOne()) != '"' && haveNext()) {
            if (peeked == '\n') { lineNum++; }
            advance();
        }

        if (!haveNext()) {
            throw new IllegalStateException("Unterminated string line: " + lineNum);
        }

        advance(); // Consume closing "

        String value = source.substring(startIndex + 1, currIndex - 1); // Trim outer " "
        addToken(TokenType.STRING, value);
    }

    public void lexNumber() {
        char litType = '\0';

        while (isNumeric(peekOne())) {
            advance();

            char nextPeek = peekOne();
            if (nextPeek == '.' && isNumeric(peekTwo())) {
                advance();
                litType = 'd';
            }
        }

        char nextPeek = peekOne();
        if (nextPeek == 'f' || nextPeek == 'F') {
            litType = 'f';
            numTermCheck.accept(peekTwo());
        } else if (nextPeek == 'l' || nextPeek == 'L') {
            numTermCheck.accept(peekTwo());
            litType = 'l';
        } else if (nextPeek == 'd' || nextPeek == 'D') {
            litType = 'd';
            numTermCheck.accept(peekTwo());
        }

        switch (litType) {
            case 'd' -> addToken(TokenType.DOUBLE, Double.parseDouble(source.substring(startIndex, currIndex)));
            case 'f' -> addToken(TokenType.FLOAT, Float.parseFloat(source.substring(startIndex, currIndex)));
            case 'l' -> addToken(TokenType.LONG, Long.parseLong(source.substring(startIndex, currIndex)));
            default -> {
                long value = Long.parseLong(source.substring(startIndex, currIndex));
                if (value < Integer.MAX_VALUE) {
                    addToken(TokenType.INT, (int) value);
                } else {
                    addToken(TokenType.LONG, value);
                }
            }
        }
        if (litType == 'f' || litType == 'l' || litType == 'd') {
            advance();
        }
    }

    public boolean lexKeywordOrIdentifier() {

        while (isAlphaNumeric(peekOne())) {
            advance();
        }

        String text = source.substring(startIndex, currIndex);
        TokenType kwToken = TokenType.matchKeyWord(text);
        addToken(Objects.requireNonNullElse(kwToken, TokenType.IDENTIFIER), text);
        return true;
    }

    private char advance() {
        return source.charAt(currIndex++);
    }

    private char peekOne() {
        return haveNext() ? source.charAt(currIndex) : '\0';
    }

    private char peekTwo() {
        return (currIndex < source.length()) ? source.charAt(currIndex + 1) : '\0';
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    public boolean isNumeric(char c) {
        return c >= '0' && c <= '9';
    }

    public boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || c == '_'
                || c == '-'; // - is not valid to start an identifier(will lex as minus), but can occur mid-identifier, change this?
    }

    private boolean isDefEnd(char c) {
        return c == ' ' || c == '\r' || c == '\n' || c == '\t';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isNumeric(c);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(startIndex, currIndex);
        tokens.add(new Token(type, text, literal, lineNum));
    }

    private boolean haveNext() {
        return currIndex != source.length();
    }

    //  Helpers
    private char[] terminalChars = new char[]{' ', ')', '}', ']'};

    private boolean isTerminalChar(char c) {
        for (char t : terminalChars) {
            if (c == t) { return true; }
            ;
        }
        return false;
    }

    private Consumer<Character> numTermCheck = (c) -> {
        if (c != ' ' && c != ')') {
            throw new IllegalStateException("Encountered data directly after numeric literal terminator on line: " + lineNum);
        }
    };

}
