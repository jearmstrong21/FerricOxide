package mackycheese21.ferricoxide.nast.hl.expr;

import mackycheese21.ferricoxide.nast.hl.HLContext;
import mackycheese21.ferricoxide.nast.hl.HLValue;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;
import mackycheese21.ferricoxide.nast.ll.expr.LLIntConstant;
import mackycheese21.ferricoxide.parser.token.Span;

public class HLIntConstant extends HLExpression {

    public final long constant;

    public HLIntConstant(Span span, long constant) {
        super(span);
        this.constant = constant;
    }

    @Override
    public void compile(HLContext ctx) {
        value = new HLValue(HLTypeId.i32(span), new LLIntConstant((int) constant));
    }

    @Override
    public String toString() {
        return "" + constant;
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
