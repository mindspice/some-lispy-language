import interpreter.ScopeEnv;
import interpreter.Interpreter;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.Scanner;


public class Repl {

    public static void main(String[] args) {
        Interpreter interpreter = new Interpreter();
        System.out.println("Lisp REPL. Type 'exit' to quit.");

        try {
            Terminal terminal = TerminalBuilder.builder().build();
            LineReader lineReader = LineReaderBuilder.builder()
                    .option(LineReader.Option.AUTO_FRESH_LINE, true)
                    .option(LineReader.Option.HISTORY_IGNORE_DUPS, true)
                    .option(LineReader.Option.AUTO_MENU, true)
                    .option(LineReader.Option.MENU_COMPLETE, true)
                    .option(LineReader.Option.LIST_PACKED, true)
                    .terminal(terminal)
                    .build();

            while (true) {
                String input = lineReader.readLine("> ");

                if ("exit".equalsIgnoreCase(input.trim())) {
                    break;
                }

                try {
                    String result = interpreter.eval(input);
                    System.out.println(result);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize terminal: " + e.getMessage());
        }

        System.out.println("REPL terminated.");
    }



//    public static void main(String[] args) {
//        Scanner scanner = new Scanner(System.in);
//        Interpreter interpreter = new Interpreter();
//        ScopeEnv globalEnv = new ScopeEnv(); // Initialize your global environment
//
//        System.out.println("Lisp REPL. Type 'exit' to quit.");
//
//        while (true) {
//            System.out.print("> ");
//            String input = scanner.nextLine();
//
//            if ("exit".equalsIgnoreCase(input)) {
//                break;
//            }
//
//            try {
//                String result = interpreter.eval(input);
//                System.out.println(result);
//            } catch (Exception e) {
//                System.out.println("Error: " + e.getMessage());
//            }
//        }
//
//        scanner.close();
//        System.out.println("REPL terminated.");
//    }
}
