package mackycheese21.ferricoxide.token;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalLong;

public class Token {
    private final String identifier;
    private final String string;
    private final double decimal;
    private final long integer;
    private final Punctuation punctuation;
    public final Type type;

    public final Span span;

    public static final String QUOTES = "\'\"";
    public static final String ALPHABET = buildAlphabet();
    public static final String DIGITS = buildDigits();
    public static final String IDENTIFIER_PUNCTUATION = "@#$";

    private static String buildAlphabet() {
        String alphabet = "";
        for (int i = 0; i < 26; i++) {
            alphabet += (char) ('A' + i);
            alphabet += (char) ('a' + i);
        }
        return alphabet;
    }

    private static String buildDigits() {
        String digits = "";
        for (int i = 0; i < 10; i++) {
            digits += i;
        }
        return digits;
    }

    public static final String IDENTIFIER_START = ALPHABET;
    public static final String IDENTIFIER_REST = IDENTIFIER_START + DIGITS + IDENTIFIER_PUNCTUATION;

    public enum Type {
        IDENTIFIER,
        STRING,
        DECIMAL,
        INTEGER,
        PUNCTUATION
    }

    public enum Punctuation {
        EQEQ("=="),
        EQ("="),
        COMMA(","),
        L_PAREN("("),
        R_PAREN(")"),
        L_BRACKET("{"),
        R_BRACKET("}"),
        PLUS("+"),
        MINUS("-"),
        STAR("*"),
        SLASH("/"),
        SEMICOLON(";");

        public final String str;

        Punctuation(String str) {
            this.str = str;
            if(str.length() == 0) throw new UnsupportedOperationException();
        }
    }

    private Token(String identifier, String string, double decimal, long integer, Punctuation punctuation, Type type, Span span) {
        this.identifier = identifier;
        this.string = string;
        this.decimal = decimal;
        this.integer = integer;
        this.punctuation = punctuation;
        this.type = type;
        this.span = span;
    }

    public static Token identifier(Span span, String identifier) {
        return new Token(identifier, null, 0, 0, null, Type.IDENTIFIER, span);
    }

    public static Token string(Span span, String string) {
        return new Token(null, string, 0, 0, null, Type.STRING, span);
    }

    public static Token decimal(Span span, double decimal) {
        return new Token(null, null, decimal, 0, null, Type.DECIMAL, span);
    }

    public static Token integer(Span span, long integer) {
        return new Token(null, null, 0, integer, null, Type.INTEGER, span);
    }

    public static Token punctuation(Span span, Punctuation punctuation) {
        return new Token(null, null, 0, 0, punctuation, Type.PUNCTUATION, span);
    }

    public String identifier() throws TokenException {
        mustBe(Type.IDENTIFIER);
        return identifier;
    }

    public String string() throws TokenException {
        mustBe(Type.STRING);
        return identifier;
    }

    public double decimal() throws TokenException {
        mustBe(Type.DECIMAL);
        return decimal;
    }

    public long integer() throws TokenException {
        mustBe(Type.INTEGER);
        return integer;
    }

    public Punctuation punctuation() throws TokenException {
        mustBe(Type.PUNCTUATION);
        return punctuation;
    }

    public Optional<String> maybeIdentifier() {
        return type == Type.IDENTIFIER ? Optional.of(identifier) : Optional.empty();
    }

    public Optional<String> maybeString() {
        return type == Type.STRING ? Optional.of(string) : Optional.empty();
    }

    public OptionalDouble maybeDecimal() {
        return type == Type.DECIMAL ? OptionalDouble.of(decimal) : OptionalDouble.empty();
    }

    public OptionalLong maybeInteger() {
        return type == Type.INTEGER ? OptionalLong.of(integer) : OptionalLong.empty();
    }

    public Optional<Punctuation> maybePunctuation() {
        return type == Type.PUNCTUATION ? Optional.of(punctuation) : Optional.empty();
    }

    public boolean is(Type... types) {
        for(Type t : types) {
            if(type == t) return true;
        }
        return false;
    }

    public boolean is(Punctuation... punctuations) throws TokenException {
        mustBe(Type.PUNCTUATION);
        for(Punctuation p : punctuations) {
            if(punctuation == p) return true;
        }
        return false;
    }

    public void mustBe(Type... types) throws TokenException {
        if(!is(types)) {
            throw new TokenException(TokenException.Type.UNEXPECTED_TOKEN, this);
        }
    }

    public void mustBe(Punctuation... punctuations) throws TokenException {
        if(!is(punctuations)) {
            throw new TokenException(TokenException.Type.UNEXPECTED_TOKEN, this);
        }
    }

    public String valueString() {
        switch (type) {
            case IDENTIFIER:
                return identifier;
            case STRING:
                return String.format("\"%s\"", string);
            case DECIMAL:
                return decimal + "";
            case INTEGER:
                return integer + "";
            case PUNCTUATION:
                return punctuation.toString();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return String.format("[%s: %s[%s]]", span, type.toString().toLowerCase(), valueString());
    }
}
