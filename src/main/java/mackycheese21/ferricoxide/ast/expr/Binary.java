package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.visitor.ExpressionRequester;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;
import mackycheese21.ferricoxide.parser.token.Span;

public class Binary extends Expression {

    public Expression a;
    public Expression b;
    public final BinaryOperator operator;

    public Binary(Span span, Expression a, Expression b, BinaryOperator operator) {
        super(span);

        this.a = a;
        this.b = b;
        this.operator = operator;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitBinary(this);
    }

    @Override
    public <T, U> T request(ExpressionRequester<T, U> requester, U request) {
        return requester.visitBinary(request, this);
    }
}
