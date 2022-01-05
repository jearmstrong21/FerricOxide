import mackycheese21.ferricoxide.SourceCodeException;
import mackycheese21.ferricoxide.parser.token.Tokenizer;

import java.nio.file.Path;

public class DebugNTokenizer {

    public static void main(String[] args) {
        String data = "fn foo(e) { 5.5 0x13.3 }";
        System.out.println(data);
        try {
            System.out.println(Tokenizer.tokenize(Path.of("e"), data));
        } catch (SourceCodeException e) {
            for (int i = 0; i < e.span.start().index(); i++) System.out.print(" ");
            for (int i = e.span.start().index(); i < e.span.end().index(); i++) System.out.print("^");
            System.out.println();
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

}
