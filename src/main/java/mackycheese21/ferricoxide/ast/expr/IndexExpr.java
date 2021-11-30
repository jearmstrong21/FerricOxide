package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;

public class IndexExpr extends Expression {

    public final Expression value;
    public final Expression index;

    public IndexExpr(Expression value, Expression index) {
        super(true);
        this.value = value;
        this.index = index;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitIndexExpr(this);
    }
}
