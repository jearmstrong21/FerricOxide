package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.visitor.ExpressionRequester;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;
import mackycheese21.ferricoxide.parser.token.Span;

public class PointerDeref extends Expression {

    public Expression deref;

    public PointerDeref(Span span, Expression deref) {
        super(span);
        this.deref = deref;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitPointerDeref(this);
    }

    @Override
    public <T, U> T request(ExpressionRequester<T, U> requester, U request) {
        return requester.visitPointerDeref(request, this);
    }
}
