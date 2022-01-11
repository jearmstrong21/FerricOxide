package mackycheese21.ferricoxide.ast.hl.expr;

import mackycheese21.ferricoxide.parser.token.Span;

public class HLStringConstant extends HLExpression {

    public final String value;

    public HLStringConstant(Span span, String value) {
        super(span);
        this.value = value;
    }

    @Override
    public <T> T visit(HLExpressionVisitor<T> visitor) {
        return visitor.visitStringConstant(this);
    }

    public static String escape(String value) {
        return value
                .replace("\"", "\\\"")
                .replace("'", "\\'")
                .replace("\0", "\\0")
                .replace("\n", "\\n")
                .replace("\t", "\\t")
                .replace("\\", "\\\\");
    }

    public static String unescape(String value) {
        return value
                .replace("\\\"", "\"")
                .replace("\\'", "'")
                .replace("\\0", "\0")
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\\\", "\\");
    }
}
