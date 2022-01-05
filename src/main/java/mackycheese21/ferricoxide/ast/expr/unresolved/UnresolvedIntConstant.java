package mackycheese21.ferricoxide.ast.expr.unresolved;

import mackycheese21.ferricoxide.ast.expr.Expression;
import mackycheese21.ferricoxide.ast.visitor.ExpressionRequester;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;
import mackycheese21.ferricoxide.parser.token.Span;

public class UnresolvedIntConstant extends Expression {

    // TODO: Hint type
    public long value;

    public UnresolvedIntConstant(Span span, long value) {
        super(span);

        this.value = value;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitUnresolvedIntConstant(this);
    }

    @Override
    public <T, U> T request(ExpressionRequester<T, U> requester, U request) {
        return requester.visitUnresolvedIntConstant(request, this);
    }
}
