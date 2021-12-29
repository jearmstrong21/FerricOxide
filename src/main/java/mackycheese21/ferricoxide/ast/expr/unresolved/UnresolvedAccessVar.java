package mackycheese21.ferricoxide.ast.expr.unresolved;

import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.expr.Expression;
import mackycheese21.ferricoxide.ast.visitor.ExpressionRequester;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;
import mackycheese21.ferricoxide.parser.token.Span;

public class UnresolvedAccessVar extends Expression {

    public boolean explicitRef;
    public Identifier identifier;

    public UnresolvedAccessVar(Span span, boolean explicitRef, Identifier identifier) {
        super(span);

        this.explicitRef = explicitRef;
        this.identifier = identifier;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitUnresolvedAccessVar(this);
    }

    @Override
    public <T, U> T request(ExpressionRequester<T, U> requester, U request) {
        return requester.visitUnresolvedAccessVar(request, this);
    }
}
