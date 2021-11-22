package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.Module;
import mackycheese21.ferricoxide.ast.token.Span;
import mackycheese21.ferricoxide.ast.token.Token;
import mackycheese21.ferricoxide.ast.token.Tokenizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class AstParserDebug {

    private static String loadFile(String filename) {
        try {
            return Files.readString(Path.of(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Span getSpan(SourceCodeException s) {
        if (s.span != null) return s.span;
        if (s.token != null) return s.token.span;
        if (s.getCause() != null && s.getCause() instanceof SourceCodeException) {
            return getSpan((SourceCodeException) s.getCause());
        }
        return null;
    }

    public static void main(String[] args) {
        String data = loadFile("BIN/main.fo");
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
        } catch (SourceCodeException e) {
            Span s = getSpan(e);
            if (s != null) {
                s.highlight(data);
//                for (int i = 0; i < s.start; i++) System.out.print(" ");
//                for (int i = s.start; i < s.end; i++) System.out.print("^");
            }
            System.out.println();
            System.out.println(e.getMessage());
            pst(e);
        }
    }

    private static void pst(Throwable e) {
        if(e.getCause() != null) {
            pst(e.getCause());
        }
        e.printStackTrace();
    }

}
