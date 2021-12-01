package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;

public class StringConstant extends Expression {

    public final String value;

    public StringConstant(String value) {
        super(false);
        this.value = value;
        System.out.println(unescape(value));
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitStringConstant(this);
    }

    public static String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\'", "\\\'")
                .replace("\0", "\\0")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    public static String unescape(String value) {
        return value.replace("\\\\", "\\")
                .replace("\\\"", "\"")
                .replace("\\\'", "\'")
                .replace("\\0", "\0")
                .replace("\\n", "\n")
                .replace("\\t", "\t");
    }

}
