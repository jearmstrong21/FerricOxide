package mackycheese21.ferricoxide.nast.hl.expr;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.UnaryOperator;
import mackycheese21.ferricoxide.nast.hl.HLContext;
import mackycheese21.ferricoxide.nast.hl.HLValue;
import mackycheese21.ferricoxide.nast.hl.type.HLPointerTypeId;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;
import mackycheese21.ferricoxide.nast.hl.type.HLTypePredicate;
import mackycheese21.ferricoxide.nast.ll.expr.LLUnary;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;

public abstract class HLExpression {

    public final Span span;

    public HLValue value;

    public HLExpression(Span span) {
        this.span = span;
    }

    public static HLExpression none(Span span) {
        return new HLZeroInit(span, HLTypeId.none(span));
    }

    public abstract void compile(HLContext ctx);

    @Override
    public abstract String toString();

    public final <T> T require(HLContext ctx, HLTypePredicate<T> predicate) {
        if(predicate.apply(value.type()) == null) {
            if (value.type() instanceof HLPointerTypeId pointer && predicate.apply(pointer.to) != null) {
                value = new HLValue(pointer.to, new LLUnary(UnaryOperator.DEREF, value.ll()));
            }
        }
        return predicate.require(value.type());
    }

    public static boolean hasForcedReturn(List<HLExpression> exprs) {
        return exprs.stream().anyMatch(HLExpression::hasForcedReturn);
    }

    public static boolean hasForcedBreak(List<HLExpression> exprs) {
        return exprs.stream().anyMatch(HLExpression::hasForcedBreak);
    }

    public abstract boolean hasForcedReturn();

    public abstract boolean hasForcedBreak();

    public final void requireLinearFlow() {
        if(hasForcedReturn() || hasForcedBreak()) throw new AnalysisException(span, "require linear flow");
    }

}
