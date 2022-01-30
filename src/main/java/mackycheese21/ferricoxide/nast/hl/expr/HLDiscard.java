package mackycheese21.ferricoxide.nast.hl.expr;

import mackycheese21.ferricoxide.nast.hl.HLContext;
import mackycheese21.ferricoxide.nast.hl.HLValue;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;
import mackycheese21.ferricoxide.nast.ll.expr.LLDiscard;
import mackycheese21.ferricoxide.parser.token.Span;

// This propogates `discard`s control flow properties because its legitimate to discard a return or break,
// although redundant since they return () anyway
public class HLDiscard extends HLExpression {

    public final HLExpression discard;

    public HLDiscard(Span span, HLExpression discard) {
        super(span);
        this.discard = discard;
    }

    @Override
    public void compile(HLContext ctx) {
        discard.compile(ctx);
        value = new HLValue(HLTypeId.none(span), new LLDiscard(discard.value.ll()));
    }

    @Override
    public String toString() {
        return "%s;".formatted(discard);
    }

    @Override
    public boolean hasForcedReturn() {
        return discard.hasForcedReturn();
    }

    @Override
    public boolean hasForcedBreak() {
        return discard.hasForcedBreak();
    }
}
