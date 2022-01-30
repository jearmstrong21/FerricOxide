package mackycheese21.ferricoxide.nast.hl.expr;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.nast.hl.HLContext;
import mackycheese21.ferricoxide.nast.hl.HLValue;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;
import mackycheese21.ferricoxide.nast.ll.expr.LLExpression;
import mackycheese21.ferricoxide.nast.ll.expr.LLNone;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;
import java.util.stream.Collectors;

public class HLBlock extends HLExpression {

    public final List<HLExpression> exprs;

    public HLBlock(Span span, List<HLExpression> exprs) {
        super(span);
        this.exprs = exprs;
    }

    @Override
    public void compile(HLContext ctx) {
        for (int i = 0; i < exprs.size() - 1; i++) {
            exprs.get(i).requireLinearFlow();
        }
        exprs.forEach(e -> e.compile(ctx));
        if (exprs.size() == 0) {
            value = new HLValue(HLTypeId.none(span), new LLNone());
        } else {
            value = exprs.get(exprs.size() - 1).value;
        }
    }

    @Override
    public String toString() {
        return "{ %s }".formatted(exprs.stream().map(HLExpression::toString).collect(Collectors.joining(" ")));
    }

    @Override
    public boolean hasForcedReturn() {
        return hasForcedReturn(exprs);
    }

    @Override
    public boolean hasForcedBreak() {
        return hasForcedBreak(exprs);
    }
}
