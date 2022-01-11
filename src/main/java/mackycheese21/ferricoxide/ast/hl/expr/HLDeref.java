package mackycheese21.ferricoxide.ast.hl.expr;

import mackycheese21.ferricoxide.parser.token.Span;

public class HLDeref extends HLExpression {

    public final HLExpression value;

    public HLDeref(Span span, HLExpression value) {
        super(span);
        this.value = value;
    }

    @Override
    public <T> T visit(HLExpressionVisitor<T> visitor) {
        return visitor.visitDeref(this);
    }
}
