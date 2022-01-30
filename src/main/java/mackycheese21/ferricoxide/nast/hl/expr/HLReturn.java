package mackycheese21.ferricoxide.nast.hl.expr;

import mackycheese21.ferricoxide.nast.hl.HLContext;
import mackycheese21.ferricoxide.nast.hl.HLValue;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;
import mackycheese21.ferricoxide.nast.ll.expr.LLReturn;
import mackycheese21.ferricoxide.parser.token.Span;

public class HLReturn extends HLExpression {

    public final HLExpression ret;

    public HLReturn(Span span, HLExpression ret) {
        super(span);
        this.ret = ret;
    }

    @Override
    public void compile(HLContext ctx) {
        ret.requireLinearFlow();

        ret.compile(ctx);
        ctx.returnType.require(ret.value.type());

        value = new HLValue(HLTypeId.none(span), new LLReturn(ret.value.ll()));
    }

    @Override
    public String toString() {
        return "return %s".formatted(ret);
    }

    @Override
    public boolean hasForcedReturn() {
        return true;
    }

    @Override
    public boolean hasForcedBreak() {
        return false;
    }
}
