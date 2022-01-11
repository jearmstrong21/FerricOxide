package mackycheese21.ferricoxide.ast.hl.expr;

import mackycheese21.ferricoxide.parser.token.Span;

public abstract class HLExpression {

    public final Span span;

    protected HLExpression(Span span) {
        this.span = span;
    }

    public abstract <T> T visit(HLExpressionVisitor<T> visitor);

}
