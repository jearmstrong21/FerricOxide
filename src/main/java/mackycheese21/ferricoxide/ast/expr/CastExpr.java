package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.type.FOType;
import mackycheese21.ferricoxide.ast.visitor.ExpressionRequester;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;
import mackycheese21.ferricoxide.parser.token.Span;

public class CastExpr extends Expression {

    public FOType target;
    public Expression value;

    public CastExpr(Span span, FOType target, Expression value) {
        super(span);

        this.target = target;
        this.value = value;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitCastExpr(this);
    }

    @Override
    public <T, U> T request(ExpressionRequester<T, U> requester, U request) {
        return requester.visitCastExpr(request, this);
    }
}
