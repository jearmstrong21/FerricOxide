package mackycheese21.ferricoxide.ast.hl.expr;

import mackycheese21.ferricoxide.ast.BinaryOperator;
import mackycheese21.ferricoxide.parser.token.Span;

public class HLBinary extends HLExpression {

    public final HLExpression left;
    public final BinaryOperator operator;
    public final HLExpression right;

    public HLBinary(Span span, HLExpression left, BinaryOperator operator, HLExpression right) {
        super(span);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public <T> T visit(HLExpressionVisitor<T> visitor) {
        return visitor.visitBinary(this);
    }
}
