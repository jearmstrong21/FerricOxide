package mackycheese21.ferricoxide.ast.hl.expr;

import mackycheese21.ferricoxide.ast.hl.type.HLType;
import mackycheese21.ferricoxide.parser.token.Span;

public class HLZeroInit extends HLExpression {

    public final HLType target;

    public HLZeroInit(Span span, HLType target) {
        super(span);
        this.target = target;
    }

    @Override
    public <T> T visit(HLExpressionVisitor<T> visitor) {
        return visitor.visitZeroInit(this);
    }
}
