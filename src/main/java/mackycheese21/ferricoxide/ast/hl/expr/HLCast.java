package mackycheese21.ferricoxide.ast.hl.expr;

import mackycheese21.ferricoxide.ast.hl.type.HLType;
import mackycheese21.ferricoxide.parser.token.Span;

public class HLCast extends HLExpression {

    public final HLExpression value;
    public HLType target;

    public HLCast(Span span, HLExpression value, HLType target) {
        super(span);
        this.value = value;
        this.target = target;
    }

    @Override
    public <T> T visit(HLExpressionVisitor<T> visitor) {
        return visitor.visitCast(this);
    }
}
