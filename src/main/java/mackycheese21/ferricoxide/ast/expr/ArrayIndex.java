package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.visitor.ExpressionRequester;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;
import mackycheese21.ferricoxide.parser.token.Span;

public class ArrayIndex extends Expression {

    public Expression array;
    public Expression index;
    public boolean ref;

    public ArrayIndex(Span span, Expression array, Expression index, boolean ref) {
        super(span);
        this.array = array;
        this.index = index;
        this.ref = ref;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitArrayIndex(this);
    }

    @Override
    public <T, U> T request(ExpressionRequester<T, U> requester, U request) {
        return requester.visitArrayIndex(request, this);
    }
}
