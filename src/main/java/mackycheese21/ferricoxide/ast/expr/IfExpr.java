package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;

public class IfExpr extends Expression {

    public final Expression condition;
    public final Expression then;
    public final Expression otherwise;

    public IfExpr(Expression condition, Expression then, Expression otherwise) {
        super(then.lvalue && otherwise.lvalue);
        this.condition = condition;
        this.then = then;
        this.otherwise = otherwise;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitIfExpr(this);
    }
}
