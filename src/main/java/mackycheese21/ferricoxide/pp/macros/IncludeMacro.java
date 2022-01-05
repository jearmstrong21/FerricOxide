package mackycheese21.ferricoxide.pp.macros;

import mackycheese21.ferricoxide.parser.token.Span;
import mackycheese21.ferricoxide.parser.token.TokenScanner;
import mackycheese21.ferricoxide.parser.token.TokenTree;
import mackycheese21.ferricoxide.pp.Macro;
import mackycheese21.ferricoxide.pp.Preprocessor;

import java.io.IOException;
import java.util.List;

public class IncludeMacro implements Macro {
    @Override
    public List<TokenTree> eval(Preprocessor preprocessor, Span argSpan, TokenScanner scanner) {
        String filename = scanner.next().requireStringLiteral().value;
        Span span = scanner.lastConsumedSpan();
        scanner.requireEmpty();
        try {
            return preprocessor.include(span, filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
