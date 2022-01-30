package mackycheese21.ferricoxide.nast.ll.expr;

import mackycheese21.ferricoxide.UnaryOperator;
import mackycheese21.ferricoxide.nast.ll.LLContext;

public class LLUnary extends LLExpression {

    public final UnaryOperator op;
    public final LLExpression a;

    public LLUnary(UnaryOperator op, LLExpression a) {
        this.op = op;
        this.a = a;
    }

    @Override
    public void compile(LLContext ctx) {
        a.compile(ctx);
        value = op.run(ctx.builder(), a.value);
    }

    @Override
    public String toString() {
        return "%s%s".formatted(op.punctuation.str, a);
    }
}
