package mackycheese21.ferricoxide;

import mackycheese21.ferricoxide.parser.token.Span;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Identifier {

    public final Span span;
    private final String[] strings;

    public Identifier(Span span, String[] strings) {
        this.span = span;
        this.strings = strings;
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

    public int length() {
        return strings.length;
    }

    public static Identifier concat(Identifier... identifiers) {
        int len = 0;
        for (Identifier id : identifiers) len += id.strings.length;
        String[] strs = new String[len];
        int i = 0;
        for (Identifier id : identifiers) {
            for (String s : id.strings) strs[i++] = s;
        }
        return new Identifier(identifiers[identifiers.length - 1].span, strs);
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
        return Arrays.stream(strings).map(string -> string + string.length()).collect(Collectors.joining("_"));
    }

    @Override
    public String toString() {
        return String.join("::", strings);
    }

    public String getLast() {
        return strings[strings.length - 1];
    }
}
