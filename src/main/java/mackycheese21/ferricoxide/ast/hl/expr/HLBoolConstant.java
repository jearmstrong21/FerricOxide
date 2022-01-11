package mackycheese21.ferricoxide.ast.hl.expr;

import mackycheese21.ferricoxide.parser.token.Span;

public class HLBoolConstant extends HLExpression {

    public final boolean value;

    public HLBoolConstant(Span span, boolean value) {
        super(span);
        this.value = value;
    }

    @Override
    public <T> T visit(HLExpressionVisitor<T> visitor) {
        return visitor.visitBoolConstant(this);
    }
}
