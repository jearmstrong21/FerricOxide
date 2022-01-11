package mackycheese21.ferricoxide.ast.ll.expr;

import mackycheese21.ferricoxide.ast.BinaryOperator;

public class LLBinary extends LLExpression {

    public final LLExpression left;
    public final BinaryOperator operator;
    public final LLExpression right;

    public LLBinary(LLExpression left, BinaryOperator operator, LLExpression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public <T> T visit(LLExpressionVisitor<T> visitor) {
        return visitor.visitBinary(this);
    }
}
