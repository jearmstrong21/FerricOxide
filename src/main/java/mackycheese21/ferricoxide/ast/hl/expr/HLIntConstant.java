package mackycheese21.ferricoxide.ast.hl.expr;

import mackycheese21.ferricoxide.parser.token.Span;

public class HLIntConstant extends HLExpression {

    public final long value;

    public HLIntConstant(Span span, long value) {
        super(span);
        this.value = value;
    }

    @Override
    public <T> T visit(HLExpressionVisitor<T> visitor) {
        return visitor.visitIntConstant(this);
    }
}
