package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.visitor.ExpressionRequester;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;

public class CallExpr extends Expression {

    public Expression function;
    public List<Expression> params;

    public CallExpr(Span span, Expression function, List<Expression> params) {
        super(span);

        this.function = function;
        this.params = params;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitCallExpr(this);
    }

    @Override
    public <T, U> T request(ExpressionRequester<T, U> requester, U request) {
        return requester.visitCallExpr(request, this);
    }
}
