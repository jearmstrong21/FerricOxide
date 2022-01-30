package mackycheese21.ferricoxide.nast.hl.expr;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.nast.hl.HLContext;
import mackycheese21.ferricoxide.nast.hl.HLValue;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;
import mackycheese21.ferricoxide.nast.ll.expr.LLLoop;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;
import java.util.stream.Collectors;

public class HLLoop extends HLExpression {

    public final List<HLExpression> exprs;

    public HLLoop(Span span, List<HLExpression> exprs) {
        super(span);
        this.exprs = exprs;
    }

    @Override
    public void compile(HLContext ctx) {
        for (int i = 0; i < exprs.size() - 1; i++) {
            exprs.get(i).requireLinearFlow();
        }
        if (exprs.size() > 0 && exprs.get(exprs.size() - 1).hasForcedReturn())
            // mainly because llvm is a bitch
            throw new AnalysisException(exprs.get(exprs.size() - 1).span, "cannot force return on last expression in loop");
        exprs.forEach(e -> e.compile(ctx));
        value = new HLValue(HLTypeId.none(span), new LLLoop(exprs.stream().map(e -> e.value.ll()).collect(Collectors.toList())));
    }

    @Override
    public String toString() {
        return "loop { %s }".formatted(exprs.stream().map(HLExpression::toString).collect(Collectors.joining(" ")));
    }

    @Override
    public boolean hasForcedReturn() {
        return hasForcedReturn(exprs);
    }

    @Override
    public boolean hasForcedBreak() {
        return false; // (palpatine) ironic
    }
}
