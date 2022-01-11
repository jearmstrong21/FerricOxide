package mackycheese21.ferricoxide.ast.hl.expr;

import mackycheese21.ferricoxide.parser.token.Span;

public class HLParen extends HLExpression {

    public final HLExpression expr;

    public HLParen(Span span, HLExpression expr) {
        super(span);
        this.expr = expr;
    }

    @Override
    public <T> T visit(HLExpressionVisitor<T> visitor) {
        return visitor.visitParen(this);
    }
}
