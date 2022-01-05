package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.type.FOType;
import mackycheese21.ferricoxide.ast.visitor.ExpressionRequester;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;
import mackycheese21.ferricoxide.parser.token.Span;

public class IntConstant extends Expression {

    public FOType type;
    public long value;

    public IntConstant(Span span, FOType type, long value) {
        super(span);

        this.type = type;
        this.value = value;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitIntConstant(this);
    }

    @Override
    public <T, U> T request(ExpressionRequester<T, U> requester, U request) {
        return requester.visitIntConstant(request, this);
    }
}
