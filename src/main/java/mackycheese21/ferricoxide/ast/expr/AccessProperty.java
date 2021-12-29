package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.visitor.ExpressionRequester;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;
import mackycheese21.ferricoxide.parser.token.Span;

public class AccessProperty extends Expression {

    public Expression aggregate;
    public int index;
    public boolean derefAggregate;
    public boolean ref;

    public AccessProperty(Span span, Expression aggregate, int index, boolean derefAggregate, boolean ref) {
        super(span);
        this.aggregate = aggregate;
        this.index = index;
        this.derefAggregate = derefAggregate;
        this.ref = ref;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitAccessProperty(this);
    }

    @Override
    public <T, U> T request(ExpressionRequester<T, U> requester, U request) {
        return requester.visitAccessProperty(request, this);
    }
}
