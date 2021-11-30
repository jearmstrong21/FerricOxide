package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;

public class PointerDeref extends Expression {

    public final Expression deref;

    public PointerDeref(Expression deref) {
        super(false);
        this.deref = deref;
    }


    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitPointerDeref(this);
    }
}
