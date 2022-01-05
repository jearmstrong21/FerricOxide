package mackycheese21.ferricoxide.parser.token;

import mackycheese21.ferricoxide.AnalysisException;

public abstract class TokenTree {

    private final Span span;
    private final String shortName;

    protected TokenTree(Span span, String shortName) {
        this.span = span;
        this.shortName = shortName;
    }

    public final Span span() {
        return span;
    }

    public GroupToken requireGroup(GroupToken.Type type) {
        throw new AnalysisException(span, "expected group, got %s".formatted(shortName));
    }

    public IdentToken requireIdent() {
        throw new AnalysisException(span, "expected ident, got %s".formatted(shortName));
    }

    public PunctToken requirePunct(PunctToken.Type type) {
        throw new AnalysisException(span, "expected punct, got %s".formatted(shortName));
    }

    public LiteralToken.String requireStringLiteral() {
        throw new AnalysisException(span, "expected string literal, got %s".formatted(shortName));
    }

}
