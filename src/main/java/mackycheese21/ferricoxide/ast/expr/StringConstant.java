package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.visitor.ExpressionRequester;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;
import mackycheese21.ferricoxide.parser.token.Span;

public class StringConstant extends Expression {

    public final String value;

    public StringConstant(Span span, String value) {
        super(span);
        this.value = value;
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

    @Override
    public <T, U> T request(ExpressionRequester<T, U> requester, U request) {
        return requester.visitStringConstant(request, this);
    }
}
