package mackycheese21.ferricoxide.nast.ll.expr;

import mackycheese21.ferricoxide.BinaryOperator;
import mackycheese21.ferricoxide.nast.ll.LLContext;

public class LLBinary extends LLExpression {

    public final BinaryOperator op;
    public final LLExpression a;
    public final LLExpression b;

    public LLBinary(BinaryOperator op, LLExpression a, LLExpression b) {
        this.op = op;
        this.a = a;
        this.b = b;
    }

    @Override
    public void compile(LLContext ctx) {
        a.compile(ctx);
        b.compile(ctx);
        value = op.run(ctx.builder(), a.value, b.value);
    }

    @Override
    public String toString() {
        return "(%s) %s (%s)".formatted(a, op.punctuation.str, b);
    }
}
