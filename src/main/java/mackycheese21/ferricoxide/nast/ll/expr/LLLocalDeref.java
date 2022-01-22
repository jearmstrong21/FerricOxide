package mackycheese21.ferricoxide.nast.ll.expr;

import mackycheese21.ferricoxide.nast.ll.LLContext;
import mackycheese21.ferricoxide.nast.ll.LLExpression;
import mackycheese21.ferricoxide.nast.ll.LLType;
import mackycheese21.ferricoxide.nast.ll.LLValue;

public class LLLocalDeref extends LLExpression {

    public final int index;

    public LLLocalDeref(int index) {
        this.index = index;
    }

    @Override
    public void compile(LLContext ctx) {
        value = new LLValue(LLType.i32(), ctx.locals().get(index).ref());
    }

    @Override
    public String toString() {
        return "*%" + index;
    }
}
