package mackycheese21.ferricoxide.nast.ll.expr;

import mackycheese21.ferricoxide.nast.ll.LLContext;

public class LLAccessLocal extends LLExpression {

    public final int index;

    public LLAccessLocal(int index) {
        this.index = index;
    }

    @Override
    public void compile(LLContext ctx) {
        value = ctx.locals().get(index);
    }

    @Override
    public String toString() {
        return "%" + index;
    }
}
