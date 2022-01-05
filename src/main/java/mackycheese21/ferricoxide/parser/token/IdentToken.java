package mackycheese21.ferricoxide.parser.token;

import mackycheese21.ferricoxide.SourceCodeException;

public class IdentToken extends TokenTree {

    public final String value;

    public IdentToken(Span span, String value) {
        super(span, "ident");
        this.value = value;
    }

    @Override
    public IdentToken requireIdent() {
        return this;
    }

    @Override
    public String toString() {
        return value;
    }

    public void requireValue(String str) {
        if(!value.equals(str)) throw new SourceCodeException(span(), "expected %s, got %s".formatted(str, value));
    }
}
