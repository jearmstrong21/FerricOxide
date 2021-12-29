package mackycheese21.ferricoxide.ast.expr.unresolved;

import mackycheese21.ferricoxide.ast.expr.Expression;
import mackycheese21.ferricoxide.ast.type.FOType;
import mackycheese21.ferricoxide.ast.visitor.ExpressionRequester;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;
import mackycheese21.ferricoxide.parser.token.Span;
import org.jetbrains.annotations.Nullable;

public class UnresolvedFloatConstant extends Expression {

    // TODO: type hint
    public double value;

    public UnresolvedFloatConstant(Span span, double value) {
        super(span);

        this.value = value;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitUnresolvedFloatConstant(this);
    }

    @Override
    public <T, U> T request(ExpressionRequester<T, U> requester, U request) {
        return requester.visitUnresolvedFloatConstant(request, this);
    }
}
