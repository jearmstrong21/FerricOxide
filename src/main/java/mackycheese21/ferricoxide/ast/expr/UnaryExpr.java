package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.UnaryOperator;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;

public class UnaryExpr extends Expression {

    public final Expression a;
    public final UnaryOperator operator;

    public UnaryExpr(Expression a, UnaryOperator operator) {
        super(false);
        this.a = a;
        this.operator = operator;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitUnaryExpr(this);
    }
}
