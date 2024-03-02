import org.testng.annotations.Test;
import parse.Lexer;
import parse.Parser;


public class ParseTest {




    @Test
    void RandomTest() {
        String s = "(define x (+ (* 10 20) (/ 10 2)))";
        var lex = new Lexer(s).start();
        var parse = new Parser(lex).start();
        System.out.println(parse);

    }
}
