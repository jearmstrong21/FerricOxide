package mackycheese21.ferricoxide.ast.hl.expr;

import mackycheese21.ferricoxide.parser.token.Span;

public class HLIfExpr extends HLExpression {

    public final HLExpression condition;
    public final HLExpression then;
    public final HLExpression otherwise;

    public HLIfExpr(Span span, HLExpression condition, HLExpression then, HLExpression otherwise) {
        super(span);
        this.condition = condition;
        this.then = then;
        this.otherwise = otherwise;
    }

    @Override
    public <T> T visit(HLExpressionVisitor<T> visitor) {
        return visitor.visitIfExpr(this);
    }
}
