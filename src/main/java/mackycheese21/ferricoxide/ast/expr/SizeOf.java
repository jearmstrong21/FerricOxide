package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.type.FOType;
import mackycheese21.ferricoxide.ast.visitor.ExpressionRequester;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;
import mackycheese21.ferricoxide.parser.token.Span;

public class SizeOf extends Expression {

    public FOType type;

    public SizeOf(Span span, FOType type) {
        super(span);

        this.type = type;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitSizeOf(this);
    }

    @Override
    public <T, U> T request(ExpressionRequester<T, U> requester, U request) {
        return requester.visitSizeOf(request, this);
    }
}
