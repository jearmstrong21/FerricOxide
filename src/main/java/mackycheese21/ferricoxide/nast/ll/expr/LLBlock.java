package mackycheese21.ferricoxide.nast.ll.expr;

import mackycheese21.ferricoxide.nast.ll.LLContext;
import mackycheese21.ferricoxide.nast.ll.LLValue;

import java.util.List;

public class LLBlock extends LLExpression {

    public final List<LLExpression> exprs;

    public LLBlock(List<LLExpression> exprs) {
        this.exprs = exprs;
    }

    @Override
    public void compile(LLContext ctx) {
        exprs.forEach(e -> e.compile(ctx));
        if (exprs.size() == 0) value = LLValue.none();
        else value = exprs.get(exprs.size() - 1).value;
    }

    @Override
    public String toString() {
        String res = "";
        for (int i = 0; i < exprs.size(); i++) {
            res += exprs.get(i);
            if (i != exprs.size() - 1) res += "; ";
        }
        return "{ %s }".formatted(res);
    }
}
