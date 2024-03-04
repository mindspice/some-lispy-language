import interpreter.Environment;
import interpreter.Interpreter;

import java.util.Scanner;


public class Repl {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Interpreter interpreter = new Interpreter();
        Environment globalEnv = new Environment(); // Initialize your global environment

        System.out.println("Lisp REPL. Type 'exit' to quit.");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();

            if ("exit".equalsIgnoreCase(input)) {
                break;
            }

            try {
                String result = interpreter.eval(input);
                System.out.println(result);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        scanner.close();
        System.out.println("REPL terminated.");
    }
}
