package mackycheese21.ferricoxide.ast.hl.expr;

import mackycheese21.ferricoxide.ast.UnaryOperator;
import mackycheese21.ferricoxide.parser.token.Span;

public class HLUnary extends HLExpression {

    public final UnaryOperator operator;
    public final HLExpression operand;

    public HLUnary(Span span, UnaryOperator operator, HLExpression operand) {
        super(span);
        this.operator = operator;
        this.operand = operand;
    }

    @Override
    public <T> T visit(HLExpressionVisitor<T> visitor) {
        return visitor.visitUnary(this);
    }
}
