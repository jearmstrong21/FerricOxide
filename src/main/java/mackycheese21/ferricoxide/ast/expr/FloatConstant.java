package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;

public class FloatConstant extends Expression {

    public final float value;

    public FloatConstant(float value) {
        super(false);
        this.value = value;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitFloatConstant(this);
    }
}
