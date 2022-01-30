package mackycheese21.ferricoxide.nast.hl.expr;

import mackycheese21.ferricoxide.nast.hl.HLContext;
import mackycheese21.ferricoxide.nast.hl.HLValue;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;
import mackycheese21.ferricoxide.nast.ll.expr.LLZeroInit;
import mackycheese21.ferricoxide.parser.token.Span;

public class HLZeroInit extends HLExpression {

    public HLTypeId type;

    public HLZeroInit(Span span, HLTypeId type) {
        super(span);
        this.type = type;
    }

    @Override
    public void compile(HLContext ctx) {
        type = ctx.resolve(type);
        value = new HLValue(type, new LLZeroInit(ctx.compile(type)));
    }

    @Override
    public String toString() {
        return "zeroinit(%s)".formatted(type);
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
