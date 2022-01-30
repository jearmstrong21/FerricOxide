package mackycheese21.ferricoxide.nast.ll.expr;

import mackycheese21.ferricoxide.nast.ll.LLContext;

import java.util.List;
import java.util.stream.Collectors;

public class LLBlock extends LLExpression {

    public final List<LLExpression> exprs;

    public LLBlock(List<LLExpression> exprs) {
        this.exprs = exprs;
    }

    @Override
    public void compile(LLContext ctx) {
        exprs.forEach(e -> e.compile(ctx));
        value = exprs.get(exprs.size() - 1).value;
    }

    @Override
    public String toString() {
        return "{ %s }".formatted(exprs.stream().map(LLExpression::toString).collect(Collectors.joining(" ")));
    }
}
