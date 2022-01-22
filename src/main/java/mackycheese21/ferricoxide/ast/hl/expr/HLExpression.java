package mackycheese21.ferricoxide.ast.hl.expr;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.ast.hl.type.HLType;
import mackycheese21.ferricoxide.parser.token.Span;

public abstract class HLExpression {

    public final Span span;
    public HLType result;

    protected HLExpression(Span span) {
        this.span = span;
    }

    public abstract <T> T visit(HLExpressionVisitor<T> visitor);

    public void requireResult(HLType type) {
        if(!result.equals(type)) throw new AnalysisException(span, "expected %s, actual %s".formatted(type, result));
    }

}
