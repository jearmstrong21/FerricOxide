package mackycheese21.ferricoxide.ast.hl.expr;

import mackycheese21.ferricoxide.parser.token.Span;

public class HLFloatConstant extends HLExpression {

    public final double value;

    public HLFloatConstant(Span span, double value) {
        super(span);
        this.value = value;
    }

    @Override
    public <T> T visit(HLExpressionVisitor<T> visitor) {
        return visitor.visitFloatConstant(this);
    }
}
