package mackycheese21.ferricoxide.parser.token;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.SourceCodeException;

import java.util.List;

public class TokenScanner {

    private final List<TokenTree> internal;
    private int index;

    public TokenScanner(List<TokenTree> internal) {
        this.internal = internal;
        index = 0;
    }

    public List<TokenTree> getInternal() {
        return internal;
    }

    public void requireEmpty() {
        if (remaining() > 0)
            throw new SourceCodeException(internal.get(internal.size() - 1).span(), "expected end of group");
    }

    public Span lastConsumedSpan() {
        return internal.get(index - 1).span();
    }

    public TokenScanner copy() {
        TokenScanner tls = new TokenScanner(internal);
        tls.index = index;
        return tls;
    }

    public void from(TokenScanner tls) {
        if (internal != tls.internal) throw new UnsupportedOperationException();
        index = tls.index;
    }

    public int remaining() {
        return internal.size() - index;
    }

    public TokenTree peek() {
        if (remaining() <= 0) {
            if (internal.size() == 0)
                throw new AnalysisException(new Span(new Span.Loc(0, 0), new Span.Loc(0, 0), null), "unexpected eof");
            throw new AnalysisException(internal.get(internal.size() - 1).span(), "unexpected eof");
        }
        return internal.get(index);
    }

    public TokenTree next() {
        TokenTree tt = peek();
        index++;
        return tt;
    }

    public boolean hasComma() {
        return remaining() > 0 && peek() instanceof PunctToken punct && punct.type == PunctToken.Type.COMMA;
    }

    public void consumeCommaIfPresent() {
        if(hasComma()) next();
    }

}
