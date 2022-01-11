package mackycheese21.ferricoxide.ast.hl.expr;

import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;

public class HLTupleInit extends HLExpression {

    public final List<HLExpression> values;

    public HLTupleInit(Span span, List<HLExpression> values) {
        super(span);
        this.values = values;
    }

    @Override
    public <T> T visit(HLExpressionVisitor<T> visitor) {
        return visitor.visitTupleInit(this);
    }
}
