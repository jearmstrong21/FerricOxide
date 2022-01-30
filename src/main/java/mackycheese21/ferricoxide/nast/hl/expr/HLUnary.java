package mackycheese21.ferricoxide.nast.hl.expr;

import mackycheese21.ferricoxide.UnaryOperator;
import mackycheese21.ferricoxide.nast.hl.HLContext;
import mackycheese21.ferricoxide.parser.token.Span;

public class HLUnary extends HLExpression {

    public final UnaryOperator op;
    public final HLExpression a;

    public HLUnary(Span span, UnaryOperator op, HLExpression a) {
        super(span);
        this.op = op;
        this.a = a;
    }

    @Override
    public void compile(HLContext ctx) {
        a.requireLinearFlow();

        a.compile(ctx);

        value = op.run(ctx, a);
    }

    @Override
    public String toString() {
        return "%s%s".formatted(op.punctuation.str, a);
    }

    @Override
    public boolean hasForcedReturn() {
        return false;
    }

    @Override
    public boolean hasForcedBreak() {
        return false;
    }
}
