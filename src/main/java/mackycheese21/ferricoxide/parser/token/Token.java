package mackycheese21.ferricoxide.parser.token;

import mackycheese21.ferricoxide.SourceCodeException;
import mackycheese21.ferricoxide.parser.AlternativeSkipException;

public class Token {
    private final String identifier;
    private final String string;
    private final double decimal;
    private final long integer;
    private final Punctuation punctuation;
    private final Keyword keyword;

    public final Type type;

    public final Span span;

    public static final String QUOTES = "\'\"";
    public static final String ALPHABET = buildAlphabet();
    public static final String DIGITS = buildDigits();
    public static final String IDENTIFIER_PUNCTUATION = "@#$_";

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

    public static final String IDENTIFIER_START = ALPHABET + IDENTIFIER_PUNCTUATION;
    public static final String IDENTIFIER_REST = IDENTIFIER_START + DIGITS;

    public String identifierOrSkip() {
        if(type == Type.IDENTIFIER) return identifier;
        throw new AlternativeSkipException();
    }

    public enum Type {
        IDENTIFIER,
        STRING,
        DECIMAL,
        INTEGER,
        PUNCTUATION,
        KEYWORD
    }

    public enum Punctuation {
//        DOUBLE_QUOTE("\""),
//        SINGLE_QUOTE("\'"),

        DOUBLE_COLON("::"),
        L_BRACE("["),
        R_BRACE("]"),
        ARROW("->"),
        PERIOD("."),
        EQEQ("=="),
        NEQ("!="),
        EQ("="),
        PERCENT("%"),
        COMMA(","),
        L_PAREN("("),
        R_PAREN(")"),
        L_BRACKET("{"),
        R_BRACKET("}"),
        PLUS("+"),
        MINUS("-"),
        STAR("*"),
        SLASH("/"),
        SEMICOLON(";"),
        COLON(":"),
        LE("<="),
        LT("<"),
        GE(">="),
        GT(">"),
        BANG("!"),
        TILDE("~"),
        ANDAND("&&"),
        AND("&"),
        OROR("||"),
        OR("|"),
        XOR("^");

        public final String str;

        Punctuation(String str) {
            this.str = str;
            if (str.length() == 0) throw new UnsupportedOperationException();
        }
    }

    public enum Keyword {
        PP_INCLUDE("#include"),
        PP_DEFINE("#define"),
        PP_IFDEF("#ifdef"),
        PP_IFNDEF("#ifndef"),
        PP_ENDIF("#endif"),

        ZEROINIT("zeroinit"),
        SIZEOF("sizeof"),

        RETURN("return"),
        EXTERN("extern"),
        EXPORT("export"),
        INLINE("inline"),

        STRUCT("struct"),
        PACKED("packed"),
        NEW("new"),
        MOD("mod"),
        AS("as"),

        TRUE("true"),
        FALSE("false"),

        FN("fn"),
        I8("i8"),
        I16("i16"),
        I32("i32"),
        I64("i64"),
        F32("f32"),
        F64("f64"),
        BOOL("bool"),
        VOID("void"),

        WHILE("while"),
        FOR("for"),
        IF("if"),
        ELSE("else");

        public final String str;

        Keyword(String str) {
            this.str = str;
            if (str.length() == 0) throw new UnsupportedOperationException();
        }
    }

    private Token(String identifier, String string, double decimal, long integer, Punctuation punctuation, Keyword keyword, Type type, Span span) {
        this.identifier = identifier;
        this.string = string;
        this.decimal = decimal;
        this.integer = integer;
        this.punctuation = punctuation;
        this.keyword = keyword;
        this.type = type;
        this.span = span;
    }

    public static Token identifier(Span span, String identifier) {
        return new Token(identifier, null, 0, 0, null, null, Type.IDENTIFIER, span);
    }

    public static Token string(Span span, String string) {
        return new Token(null, string, 0, 0, null, null, Type.STRING, span);
    }

    public static Token decimal(Span span, double decimal) {
        return new Token(null, null, decimal, 0, null, null, Type.DECIMAL, span);
    }

    public static Token integer(Span span, long integer) {
        return new Token(null, null, 0, integer, null, null, Type.INTEGER, span);
    }

    public static Token punctuation(Span span, Punctuation punctuation) {
        return new Token(null, null, 0, 0, punctuation, null, Type.PUNCTUATION, span);
    }

    public static Token keyword(Span span, Keyword keyword) {
        return new Token(null, null, 0, 0, null, keyword, Type.KEYWORD, span);
    }

    public String identifier() {
        mustBe(Type.IDENTIFIER);
        return identifier;
    }

    public String string() {
        mustBe(Type.STRING);
        return string;
    }

    public double decimal() {
        mustBe(Type.DECIMAL);
        return decimal;
    }

    public long integer() {
        mustBe(Type.INTEGER);
        return integer;
    }

    public Punctuation punctuation() {
        mustBe(Type.PUNCTUATION);
        return punctuation;
    }

    public Keyword keyword() {
        mustBe(Type.KEYWORD);
        return keyword;
    }

    public String stringOrSkip() {
        if(type != Type.STRING) throw new AlternativeSkipException();
        return string;
    }

    public Punctuation punctuationOrSkip() {
        if (type != Type.PUNCTUATION) throw new AlternativeSkipException();
        return punctuation;
    }

    public boolean is(Type... types) {
        for (Type t : types) {
            if (type == t) return true;
        }
        return false;
    }

    public boolean is(Punctuation... punctuations) {
        if (!is(Type.PUNCTUATION)) return false;
        for (Punctuation p : punctuations) {
            if (punctuation == p) return true;
        }
        return false;
    }

    public boolean is(Keyword... keywords) {
        if (!is(Type.KEYWORD)) return false;
        for (Keyword k : keywords) {
            if (keyword == k) return true;
        }
        return false;
    }

    public void mustBe(Type... types) {
        if (!is(types)) {
            throw SourceCodeException.expectedToken(this, types);
        }
    }

    public void mustBe(Punctuation... punctuations) {
        if (!is(punctuations)) {
            throw SourceCodeException.expectedToken(this, punctuations);
        }
    }

    public void mustBe(Keyword... keywords) {
        if (!is(keywords)) {
            throw SourceCodeException.expectedToken(this, keywords);
        }
    }

    public void skipIfNot(Type... types) {
        if (!is(types)) throw new AlternativeSkipException();
    }

    public Punctuation skipIfNot(Punctuation... punctuations) {
        if (!is(punctuations)) throw new AlternativeSkipException();
        return punctuation;
    }

    public void skipIfNot(Keyword... keywords) {
        if (!is(keywords)) throw new AlternativeSkipException();
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
            case KEYWORD:
                return keyword.toString();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return String.format("[%s: %s[%s]]", span, type.toString().toLowerCase(), valueString());
    }
}
