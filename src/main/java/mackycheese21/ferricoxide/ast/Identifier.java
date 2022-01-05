package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.parser.token.Span;
import mackycheese21.ferricoxide.parser.token.Tokenizer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Identifier {

    public final Span span;
    private final String[] strings;
//    public final boolean global;

    public Identifier(Span span, String[] strings) {
        this.span = span;
        this.strings = strings;
//        this.global = global;
        for (String str : strings) {
            validate(str);
        }
    }

    public Identifier(Span span, List<String> strings) {
        this(span, strings.toArray(String[]::new));
    }

    public Identifier(Span span, String string) {
        this(span, new String[]{string});
    }

    public Identifier removeLast() {
        if (strings.length == 0) throw new UnsupportedOperationException();
        return new Identifier(span, Arrays.copyOfRange(strings, 0, strings.length - 1));
    }

    private static void validate(String str) {
        if (!Tokenizer.IDENT_START.contains("" + str.charAt(0))) throw new RuntimeException();
        for (int i = 1; i < str.length(); i++) {
            if (!Tokenizer.IDENT_MID.contains("" + str.charAt(i))) throw new RuntimeException();
        }
    }

    public static Identifier concat(Identifier... identifiers) {
        int len = 0;
        for (Identifier id : identifiers) len += id.strings.length;
        String[] strs = new String[len];
        Span span = null;
        int i = 0;
        for (Identifier id : identifiers) {
            for (String s : id.strings) strs[i++] = s;
            span = Span.concat(span, id.span);
        }
        return new Identifier(span, strs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identifier that = (Identifier) o;
        return Arrays.equals(strings, that.strings);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(strings);
    }

    public String toLLVMString() {
        return "FO_" + Arrays.stream(strings).map(string -> string.length() + string).collect(Collectors.joining("__"));
    }

    @Override
    public String toString() {
        return String.join("::", strings);
    }
}
