package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.ast.token.Span;
import mackycheese21.ferricoxide.ast.token.Token;
import mackycheese21.ferricoxide.ast.token.TokenScanner;

public class SourceCodeException extends Exception {

    public enum Type {
        UNEXPECTED_TOKEN("unexpected token"),
        UNEXPECTED_CHAR("unexpected char"),
        UNEXPECTED_EOF("unexpected eof"),

        EXPECTED_EXPR("expected expr"),
        EXPECTED_ELSE("expected else"),
        EXPECTED_TYPE("expected type"),
        EMPTY_UNWRAP("empty unwrap");
        private final String msg;

        Type(String msg) {
            this.msg = msg;
        }
    }

    public final Type type;
    public final Token token;
    public final Span span;

    public SourceCodeException(Type type, TokenScanner scanner) {
        this(type, scanner.currentOrLast());
    }

    public SourceCodeException(Type type, Token token) {
        super(String.format("%s: %s", type.msg, token));
        this.type = type;
        this.token = token;
        this.span = token.span;
    }

    public SourceCodeException(Type type, Span span) {
        super(String.format("%s: %s", type.msg, span));
        this.type = type;
        this.token = null;
        this.span = span;
    }

    public SourceCodeException(Type type, SourceCodeException parent) {
        super(String.format("%s: %s", type.msg, parent == null ? "<null>" : parent.getMessage()), parent);
        this.type = type;
        this.token = null;
        this.span = null;
    }

}
