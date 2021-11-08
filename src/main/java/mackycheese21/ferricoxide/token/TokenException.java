package mackycheese21.ferricoxide.token;

public class TokenException extends Exception {

    public enum Type {
        UNEXPECTED_TOKEN("unexpected token"),
        UNEXPECTED_CHAR("unexpected char"),
        UNEXPECTED_EOF("unexpected eof");
        private final String msg;

        Type(String msg) {
            this.msg = msg;
        }
    }

    public final Type type;
    public final Token token;
    public final Span span;

    public TokenException(Type type, Token token) {
        super(String.format("%s: %s", type.msg, token));
        this.type = type;
        this.token = token;
        this.span = token.span;
    }

    public TokenException(Type type, Span span) {
        super(String.format("%s: %s", type.msg, span));
        this.type = type;
        this.token = null;
        this.span = span;
    }

}
