package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;

public class BinaryExpr extends Expression {

    public final Expression a;
    public final Expression b;
    public final BinaryOperator operator;

    public BinaryExpr(Expression a, Expression b, BinaryOperator operator) {
        super(false);
        this.a = a;
        this.b = b;
        this.operator = operator;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitBinaryExpr(this);
    }

}
