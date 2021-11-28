package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;

public class BoolConstant extends Expression {

    public final boolean value;

    public BoolConstant(boolean value) {
        super(false);
        this.value = value;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitBoolConstant(this);
    }

}
