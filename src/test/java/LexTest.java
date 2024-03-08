
import static org.junit.Assert.*;

import org.testng.annotations.Test;
import parse.Lexer;
import parse.token.TokenType;


public class LexTest {

    @Test
    public void testNumberLexing() {
        var lex = "(* 2000.00  100 200f 1l 99d 2.0 0)";
        var tokens = new Lexer().process(lex);
        System.out.println(tokens);
        assertEquals(TokenType.Literal.DOUBLE, tokens.get(2).type());
        assertEquals(2000.00, tokens.get(2).literal());

        assertEquals(TokenType.Literal.INT, tokens.get(3).type());
        assertEquals(100, tokens.get(3).literal());

        assertEquals(TokenType.Literal.FLOAT, tokens.get(4).type());
        assertEquals(200.00f, tokens.get(4).literal());

        assertEquals(TokenType.Literal.LONG, tokens.get(5).type());
        assertEquals(1L, tokens.get(5).literal());

        assertEquals(TokenType.Literal.DOUBLE, tokens.get(6).type());
        assertEquals(99.0D, tokens.get(6).literal());

        assertEquals(TokenType.Literal.DOUBLE, tokens.get(7).type());
        assertEquals(2.0, tokens.get(7).literal());

        assertEquals(TokenType.Literal.INT, tokens.get(8).type());
        assertEquals(0, tokens.get(8).literal());

        assertEquals(TokenType.Lexical.RIGHT_PAREN, tokens.get(9).type());
    }

    @Test
    public void testStringLexing() {
        var lex = "(def x \"String Literal\")";
        var tokens = new Lexer().process(lex);
        assertEquals("String Literal", tokens.get(3).literal());

        var lex2 = "(def x \"String\nLiteral\")";
        var tokens2 = new Lexer().process(lex2);
        assertEquals("String\nLiteral", tokens2.get(3).literal());

//        var lex3 = new Lexer("(def x \"String Literal\"with nested\" string\")");
//        var tokens3 = lex3.start();
//        tokens3.forEach(System.out::println);
//        assertEquals("String Literal\"with nested\" string", tokens3.get(3).literal());

    }

    @Test
    public void testKeywordAndIdentityLexing() {
        var lex = """
                (define x 10)
                (for-i 0 10 2)
                (TestFunction1)
                (testFunction2)
                (test-Function3)
                """;
        var tokens = new Lexer().process(lex);

        assertEquals(TokenType.Definition.DEFINE, tokens.get(1).type());
        assertEquals(TokenType.Literal.IDENTIFIER, tokens.get(2).type());

        assertEquals(TokenType.Expression.FOR_I, tokens.get(6).type());
        assertEquals(TokenType.Literal.IDENTIFIER, tokens.get(12).type());
        assertEquals(TokenType.Literal.IDENTIFIER, tokens.get(15).type());
        assertEquals(TokenType.Literal.IDENTIFIER, tokens.get(18).type());

    }

    @Test
    public void typeLexing() {
        var lex = "(define x ::int 10)";
        var tokens = new Lexer().process(lex);
        tokens.forEach(t -> System.out.println(t.type() + " | " + t.literal()));
    }
//
//    @Test
//    public void testQuote() {
//        var lex = "`testStringQUote";
//        var tokens = new Lexer().process(lex);
//        System.out.println(tokens);
//        assertEquals(TokenType.Literal.QUOTED, tokens.get(0).type());
//        assertEquals(lex.substring(1), tokens.get(0).literal());
//
//        lex = "`((nest)(list)(test))";
//        tokens = new Lexer().process(lex);
//        System.out.println(tokens);
//        assertEquals(TokenType.Literal.QUOTED, tokens.get(0).type());
//        assertEquals(lex.substring(1), tokens.get(0).literal());
//
//    }

}
