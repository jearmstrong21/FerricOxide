package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.visitor.ExpressionRequester;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;
import mackycheese21.ferricoxide.parser.token.Span;

public class Unary extends Expression {

    public Expression a;
    public final UnaryOperator operator;

    public Unary(Span span, Expression a, UnaryOperator operator) {
        super(span);
        this.a = a;
        this.operator = operator;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitUnary(this);
    }

    @Override
    public <T, U> T request(ExpressionRequester<T, U> requester, U request) {
        return requester.visitUnary(request, this);
    }
}
