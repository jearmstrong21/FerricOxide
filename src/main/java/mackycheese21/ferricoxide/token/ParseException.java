package mackycheese21.ferricoxide.token;

public class ParseException extends Exception {

    public enum Type {
        EXPECTED_TYPE("expected type"),
        EXPECTED_SIMPLE("expected simple"),
        INTERNAL("you shouldnt see this"),
        EXPECTED_ELSE("if statements currently require else clauses");

        private final String msg;

        Type(String msg) {
            this.msg = msg;
        }
    }

    public final Type type;
    public final Token token;

    public ParseException(Type type, TokenScanner s) {
        this(type, s.currentOrLast());
    }

    public ParseException(Type type, Token token) {
        super(String.format("%s: %s", type.msg, token));
        this.type = type;
        this.token = token;
    }

}
