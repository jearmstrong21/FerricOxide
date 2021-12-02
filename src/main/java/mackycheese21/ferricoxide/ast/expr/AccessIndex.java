package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;

public class AccessIndex extends Expression {

    public final Expression value;
    public final Expression index;

    public AccessIndex(Expression value, Expression index) {
        super(false);
        this.value = value;
        this.index = index;
    }

    @Override
    public Expression makeLValue() {
        return new RefAccessIndex(value, index);
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitAccessIndex(this);
    }
}