package mackycheese21.ferricoxide.ast.ll.expr;

import mackycheese21.ferricoxide.ast.ll.type.LLType;

public class LLFloatConstant extends LLExpression {

    public final double value;
    public final LLType target;

    public LLFloatConstant(double value, LLType target) {
        this.value = value;
        this.target = target;
    }

    @Override
    public <T> T visit(LLExpressionVisitor<T> visitor) {
        return visitor.visitFloatConstant(this);
    }
}
