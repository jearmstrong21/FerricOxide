package mackycheese21.ferricoxide.nast.hl.expr;

import mackycheese21.ferricoxide.nast.hl.HLContext;
import mackycheese21.ferricoxide.nast.hl.HLValue;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;
import mackycheese21.ferricoxide.nast.ll.expr.LLFloatConstant;
import mackycheese21.ferricoxide.parser.token.Span;

public class HLFloatConstant extends HLExpression {

    public final double constant;

    public HLFloatConstant(Span span, double constant) {
        super(span);
        this.constant = constant;
    }

    @Override
    public void compile(HLContext ctx) {
        value = new HLValue(HLTypeId.f32(span), new LLFloatConstant(constant));
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
