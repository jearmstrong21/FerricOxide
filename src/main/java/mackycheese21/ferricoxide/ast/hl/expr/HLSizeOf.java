package mackycheese21.ferricoxide.ast.hl.expr;

import mackycheese21.ferricoxide.ast.hl.type.HLType;
import mackycheese21.ferricoxide.parser.token.Span;

public class HLSizeOf extends HLExpression {

    public final HLType target;

    public HLSizeOf(Span span, HLType target) {
        super(span);
        this.target = target;
    }

    @Override
    public <T> T visit(HLExpressionVisitor<T> visitor) {
        return visitor.visitSizeOf(this);
    }
}
