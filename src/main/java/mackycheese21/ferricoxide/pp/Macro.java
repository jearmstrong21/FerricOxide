package mackycheese21.ferricoxide.pp;

import mackycheese21.ferricoxide.parser.token.Span;
import mackycheese21.ferricoxide.parser.token.TokenScanner;
import mackycheese21.ferricoxide.parser.token.TokenTree;

import java.util.List;

// TODO Span.alternate for finding error spans both in original code and inside macro
public interface Macro {

    List<TokenTree> eval(Preprocessor preprocessor, Span argSpan, TokenScanner scanner);

}
