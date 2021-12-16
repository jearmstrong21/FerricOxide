package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.parser.token.Token;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Identifier {

    private final String[] strings;
    public final boolean global;

    public Identifier(String[] strings, boolean global) {
        this.strings = strings;
        this.global = global;
        for (String str : strings) {
            validate(str);
        }
    }

    public Identifier(String string, boolean global) {
        this(new String[]{string}, global);
    }

    private static void validate(String str) {
        if (!Token.IDENTIFIER_START.contains("" + str.charAt(0))) throw new RuntimeException();
        for (int i = 1; i < str.length(); i++) {
            if (!Token.IDENTIFIER_REST.contains("" + str.charAt(i))) throw new RuntimeException();
        }
    }

    public static Identifier concat(boolean global, Identifier... identifiers) {
        int len = 0;
        for (Identifier id : identifiers) len += id.strings.length;
        String[] strs = new String[len];
        int i = 0;
        for (Identifier id : identifiers) {
            for (String s : id.strings) strs[i++] = s;
        }
        return new Identifier(strs, global);
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
