import org.testng.annotations.Test;
import parse.Lexer;
import parse.Parser;


public class ParseTest {




    @Test
    void RandomTest() {

        String s = """
                (func my-function &stat &sync (arg1 ::int &opt arg2=2 ::int)
                 (* 2 2) ::int )
                """;
        var lex = new Lexer(s).start();
        var parse = new Parser(lex).start();
        System.out.println(parse);

    }
}
