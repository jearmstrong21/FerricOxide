package mackycheese21.ferricoxide.nast.hl.expr;

import mackycheese21.ferricoxide.BinaryOperator;
import mackycheese21.ferricoxide.nast.hl.HLContext;
import mackycheese21.ferricoxide.nast.hl.HLLocal;
import mackycheese21.ferricoxide.nast.hl.HLValue;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;
import mackycheese21.ferricoxide.nast.ll.expr.LLAccessLocal;
import mackycheese21.ferricoxide.nast.ll.expr.LLBinary;
import mackycheese21.ferricoxide.nast.ll.expr.LLDiscard;
import mackycheese21.ferricoxide.parser.token.Span;

public class HLDeclareLocal extends HLExpression {

    public final String name;
    public HLTypeId type;
    public final HLExpression expr;

    public HLDeclareLocal(Span span, String name, HLTypeId type, HLExpression expr) {
        super(span);
        this.name = name;
        this.type = type;
        this.expr = expr;
    }

    @Override
    public void compile(HLContext ctx) {
        expr.requireLinearFlow();

        type = ctx.resolve(type);
        expr.compile(ctx);

        int index = ctx.localList.size();
        ctx.localStack.put(name, new HLLocal(index, type));
        ctx.localList.add(ctx.compile(type));
        value = new HLValue(HLTypeId.none(span), new LLDiscard(new LLBinary(BinaryOperator.ASSIGN, new LLAccessLocal(index), expr.value.ll())));
    }

    @Override
    public String toString() {
        return "let %s: %s = %s".formatted(name, type, value);
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
