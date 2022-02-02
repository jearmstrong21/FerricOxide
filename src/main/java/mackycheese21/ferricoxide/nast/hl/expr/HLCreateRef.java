package mackycheese21.ferricoxide.nast.hl.expr;

import mackycheese21.ferricoxide.BinaryOperator;
import mackycheese21.ferricoxide.nast.hl.HLContext;
import mackycheese21.ferricoxide.nast.hl.HLValue;
import mackycheese21.ferricoxide.nast.hl.type.HLPointerTypeId;
import mackycheese21.ferricoxide.nast.ll.expr.LLAccessLocal;
import mackycheese21.ferricoxide.nast.ll.expr.LLBinary;
import mackycheese21.ferricoxide.nast.ll.expr.LLBlock;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;

// &x <=> { let _x: typeof(x) = x; _x }
public class HLCreateRef extends HLExpression {

    public final HLExpression expr;

    public HLCreateRef(Span span, HLExpression expr) {
        super(span);
        this.expr = expr;
    }

    @Override
    public void compile(HLContext ctx) {
        if (expr instanceof HLAccessIdentifier) {
            expr.compile(ctx);
            value = new HLValue(expr.value.type(), expr.value.ll());
        } else {
            expr.requireLinearFlow();
            expr.compile(ctx);
            value = apply(span, ctx, expr.value);
        }
    }

    public static HLValue apply(Span span, HLContext ctx, HLValue value) {
        int n = ctx.localList.size();
        ctx.localList.add(ctx.compile(value.type()));
        return new HLValue(new HLPointerTypeId(span, value.type()), new LLBlock(List.of(
                new LLBinary(BinaryOperator.ASSIGN, new LLAccessLocal(n), value.ll()),
                new LLAccessLocal(n)
        )));
    }

    @Override
    public String toString() {
        return "&" + expr;
    }

    @Override
    public boolean hasForcedReturn() {
        return expr.hasForcedReturn();
    }

    @Override
    public boolean hasForcedBreak() {
        return expr.hasForcedBreak();
    }
}
