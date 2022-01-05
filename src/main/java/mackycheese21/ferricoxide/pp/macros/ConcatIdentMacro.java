package mackycheese21.ferricoxide.pp.macros;

import mackycheese21.ferricoxide.parser.token.IdentToken;
import mackycheese21.ferricoxide.parser.token.Span;
import mackycheese21.ferricoxide.parser.token.TokenScanner;
import mackycheese21.ferricoxide.parser.token.TokenTree;
import mackycheese21.ferricoxide.pp.Macro;
import mackycheese21.ferricoxide.pp.Preprocessor;

import java.util.List;

public class ConcatIdentMacro implements Macro {
    @Override
    public List<TokenTree> eval(Preprocessor preprocessor, Span argSpan, TokenScanner scanner) {
        StringBuilder res = new StringBuilder();
        Span span = null;
        while (scanner.remaining() > 0) {
            IdentToken ident = scanner.next().requireIdent();
            res.append(ident.value);
            span = Span.concat(span, ident.span());
        }
        scanner.requireEmpty();
        return List.of(new IdentToken(span, res.toString()));
    }
}
