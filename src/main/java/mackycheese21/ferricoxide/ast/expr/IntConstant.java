package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;

public class IntConstant extends Expression {

    public final int value;

    public IntConstant(int value) {
        super(false);
        this.value = value;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitIntConstant(this);
    }
}
