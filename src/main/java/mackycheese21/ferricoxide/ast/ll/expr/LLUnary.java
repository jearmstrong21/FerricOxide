package mackycheese21.ferricoxide.ast.ll.expr;

import mackycheese21.ferricoxide.ast.UnaryOperator;

public class LLUnary extends LLExpression {

    public final UnaryOperator operator;
    public final LLExpression operand;

    public LLUnary(UnaryOperator operator, LLExpression operand) {
        this.operator = operator;
        this.operand = operand;
    }

    @Override
    public <T> T visit(LLExpressionVisitor<T> visitor) {
        return visitor.visitUnary(this);
    }
}
