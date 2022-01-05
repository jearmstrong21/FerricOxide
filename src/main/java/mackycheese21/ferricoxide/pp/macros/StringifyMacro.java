package mackycheese21.ferricoxide.pp.macros;

import mackycheese21.ferricoxide.parser.token.LiteralToken;
import mackycheese21.ferricoxide.parser.token.Span;
import mackycheese21.ferricoxide.parser.token.TokenScanner;
import mackycheese21.ferricoxide.parser.token.TokenTree;
import mackycheese21.ferricoxide.pp.Macro;
import mackycheese21.ferricoxide.pp.Preprocessor;

import java.util.List;

public class StringifyMacro implements Macro {
    @Override
    public List<TokenTree> eval(Preprocessor preprocessor, Span argSpan, TokenScanner scanner) {
        StringBuilder str = new StringBuilder();
        while(scanner.remaining() > 0) {
            if(str.length() > 0) {
                str.append(" ");
            }
            str.append(scanner.next());
        }
        return List.of(new LiteralToken.String(argSpan, str.toString()));
    }
}
