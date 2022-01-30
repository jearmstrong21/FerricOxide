package mackycheese21.ferricoxide.nast.hl.expr;

import mackycheese21.ferricoxide.BinaryOperator;
import mackycheese21.ferricoxide.nast.hl.HLContext;
import mackycheese21.ferricoxide.nast.hl.type.HLTypePredicate;
import mackycheese21.ferricoxide.parser.token.Span;

public class HLBinary extends HLExpression {

    public final BinaryOperator op;
    public final HLExpression a;
    public final HLExpression b;

    public HLBinary(Span span, BinaryOperator op, HLExpression a, HLExpression b) {
        super(span);
        this.op = op;
        this.a = a;
        this.b = b;
    }

    @Override
    public void compile(HLContext ctx) {
        a.requireLinearFlow();
        b.requireLinearFlow();
        a.compile(ctx);
        b.compile(ctx);

        value = op.run(ctx, a, b);
    }

    @Override
    public String toString() {
        return "(%s) %s (%s)".formatted(a, op.punctuation.str, b);
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
