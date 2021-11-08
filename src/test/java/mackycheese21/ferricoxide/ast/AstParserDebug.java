package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.Module;
import mackycheese21.ferricoxide.token.*;

import java.util.List;

public class AstParserDebug {

    private static void handle(String msg, Span span) {
        for (int i = 0; i < span.start; i++) System.out.print(" ");
        for (int i = span.start; i < span.end; i++) System.out.print("^");
        System.out.println();
        System.out.println(msg);
    }

    public static void main(String[] args) {
//        String data = "i32 f(i32 n) { n * 3 + 4 }";
        String data = "i32 fib(i32 a, i32 b, i32 n) { n }";
        System.out.println(data);
        try {
            List<Token> tokens = Tokenizer.tokenize(data);
            Module module = AstParser.parse(tokens);
            for (Module.FunctionDecl entry : module.functions.values()) {
                System.out.println(entry.name);
                System.out.println(entry.params);
                System.out.println(entry.body);
                System.out.println();
            }
        } catch (TokenException e) {
            handle(e.getMessage(), e.span);
        } catch (ParseException e) {
            handle(e.getMessage(), e.token.span);
        }
    }

}
