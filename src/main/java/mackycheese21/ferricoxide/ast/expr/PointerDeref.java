package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;
import mackycheese21.ferricoxide.ast.visitor.StringifyVisitor;

public class PointerDeref extends Expression {

    public final Expression deref;

    public PointerDeref(Expression deref) {
        super(false);
        this.deref = deref;
    }

    @Override
    public Expression makeLValue() {
        System.out.println("makeLValue to " + deref.visit(new StringifyVisitor("")));
        if (deref.lvalue) return deref;
        else return deref.makeLValue();
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitPointerDeref(this);
    }
}
