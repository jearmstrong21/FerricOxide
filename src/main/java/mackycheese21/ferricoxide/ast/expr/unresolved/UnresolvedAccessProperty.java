package mackycheese21.ferricoxide.ast.expr.unresolved;

import mackycheese21.ferricoxide.ast.expr.Expression;
import mackycheese21.ferricoxide.ast.type.FOType;
import mackycheese21.ferricoxide.ast.visitor.ExpressionRequester;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;
import mackycheese21.ferricoxide.parser.token.Span;

public class UnresolvedAccessProperty extends Expression {

    public Expression aggregate;
    public FOType.Access access;
    public boolean arrowAccess;
    public boolean explicitRef;

    public UnresolvedAccessProperty(Span span, Expression aggregate, FOType.Access access, boolean arrowAccess, boolean explicitRef) {
        super(span);

        this.aggregate = aggregate;
        this.access = access;
        this.arrowAccess = arrowAccess;
        this.explicitRef = explicitRef;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitUnresolvedAccessProperty(this);
    }

    @Override
    public <T, U> T request(ExpressionRequester<T, U> requester, U request) {
        return requester.visitUnresolvedAccessProperty(request, this);
    }
}
