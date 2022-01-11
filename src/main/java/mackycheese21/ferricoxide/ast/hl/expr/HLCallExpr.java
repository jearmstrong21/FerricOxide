package mackycheese21.ferricoxide.ast.hl.expr;

import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;

public class HLCallExpr extends HLExpression {

    public final HLExpression function;
    public final List<HLExpression> params;

    public HLCallExpr(Span span, HLExpression function, List<HLExpression> params) {
        super(span);
        this.function = function;
        this.params = params;
    }

    @Override
    public <T> T visit(HLExpressionVisitor<T> visitor) {
        return visitor.visitCallExpr(this);
    }
}
