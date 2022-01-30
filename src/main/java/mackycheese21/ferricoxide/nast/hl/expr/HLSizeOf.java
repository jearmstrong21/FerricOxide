package mackycheese21.ferricoxide.nast.hl.expr;

import mackycheese21.ferricoxide.nast.hl.HLContext;
import mackycheese21.ferricoxide.nast.hl.HLValue;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;
import mackycheese21.ferricoxide.nast.ll.expr.LLSizeOf;
import mackycheese21.ferricoxide.parser.token.Span;

public class HLSizeOf extends HLExpression {

    public HLTypeId type;

    public HLSizeOf(Span span, HLTypeId type) {
        super(span);
        this.type = type;
    }

    @Override
    public void compile(HLContext ctx) {
        type = ctx.resolve(type);
        value = new HLValue(HLTypeId.i32(span), new LLSizeOf(ctx.compile(type)));
    }

    @Override
    public String toString() {
        return "sizeof(%s)".formatted(type);
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
