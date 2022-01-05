package mackycheese21.ferricoxide.parser.token;

import mackycheese21.ferricoxide.AnalysisException;

import java.util.List;
import java.util.stream.Collectors;

public class GroupToken extends TokenTree {

    public enum Type {
        PAREN('(', ')'), // ()
        BRACKET('[', ']'), // []
        CURLY_BRACKET('{', '}'); // {}

        public final char left, right;

        Type(char left, char right) {
            this.left = left;
            this.right = right;
        }
    }

    public final Type type;
    public final List<TokenTree> value;

    public GroupToken(Span span, Type type, List<TokenTree> value) {
        super(span, "group");
        this.type = type;
        this.value = value;
    }

    @Override
    public GroupToken requireGroup(Type type) {
        if (type == null || this.type == type) return this;
        throw new AnalysisException(span(), "expected %s, got %s".formatted(type, this.type));
    }

    @Override
    public String toString() {
        return "%s %s %s".formatted(type.left,
                value.stream().map(Object::toString).collect(Collectors.joining(" ")),
                type.right);
    }
}
