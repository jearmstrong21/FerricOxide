package mackycheese21.ferricoxide.nast.hl.expr;

import mackycheese21.ferricoxide.nast.hl.HLContext;
import mackycheese21.ferricoxide.nast.hl.HLValue;
import mackycheese21.ferricoxide.nast.ll.expr.LLIfExpr;
import mackycheese21.ferricoxide.parser.token.Span;

public class HLIfExpr extends HLExpression {

    public final HLExpression condition, then, otherwise;

    public HLIfExpr(Span span, HLExpression condition, HLExpression then, HLExpression otherwise) {
        super(span);
        this.condition = condition;
        this.then = then;
        this.otherwise = otherwise;
    }

    @Override
    public void compile(HLContext ctx) {
        condition.requireLinearFlow();

        condition.compile(ctx);
        then.compile(ctx);
        otherwise.compile(ctx);

        otherwise.value.type().pred().require(ctx, then.value.type());

        value = new HLValue(then.value.type(), new LLIfExpr(condition.value.ll(), then.value.ll(), otherwise.value.ll()));
    }

    @Override
    public String toString() {
        return "if %s { %s } else { %s }".formatted(condition, then, otherwise);
    }

    @Override
    public boolean hasForcedReturn() {
        return then.hasForcedReturn() && otherwise.hasForcedReturn();
    }

    @Override
    public boolean hasForcedBreak() {
        return otherwise.hasForcedBreak() && otherwise.hasForcedBreak();
    }
}
