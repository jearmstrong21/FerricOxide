package mackycheese21.ferricoxide.parser.token;

import mackycheese21.ferricoxide.AnalysisException;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class PunctToken extends TokenTree {

    public enum Type {
        R_ARROW("->"),

        PLUS("+"),
        MINUS("-"),
        ASTERISK("*"),
        SLASH("/"),
        PERCENT("%"),

        ANDAND("&&"),
        AND("&"),

        OROR("||"),
        OR("|"),

        XOR("^"),

        COMMA(","),
        DOT("."),

        LT_EQ("<="),
        LT("<"),

        GT_EQ(">="),
        GT(">"),

        EQEQ("=="),
        NOTEQ("!="),

        EQ("="),
        NOT("!"),
        TILDE("~"),

        COLONCOLON("::"),
        COLON(":"),
        SEMICOLON(";"),
        ;

        public final String str;

        Type(String str) {
            this.str = str;
        }
    }

    private static String computeAllPunct() {
        Set<Character> s = new HashSet<>();
        for (Type type : Type.values()) {
            for (char c : type.str.toCharArray()) {
                s.add(c);
            }
        }
        return s.stream().sorted().map(c -> "" + c).collect(Collectors.joining());
    }

    public static final String ALL_PUNCT = "(){}[]" + computeAllPunct();

    public final Type type;

    public PunctToken(Span span, Type type) {
        super(span, "punct");
        this.type = type;
    }

    @Override
    public PunctToken requirePunct(Type type) {
        if (this.type == type) return this;
        throw new AnalysisException(span(), "expected %s, got %s".formatted(type, this.type));
    }

    @Override
    public String toString() {
        return type.str;
    }
}
