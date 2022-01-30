package mackycheese21.ferricoxide.nast.ll.expr;

import mackycheese21.ferricoxide.nast.ll.LLContext;
import mackycheese21.ferricoxide.nast.ll.LLType;
import mackycheese21.ferricoxide.nast.ll.LLValue;

import java.util.List;

public abstract class LLExpression {

    public LLValue value;

    public abstract void compile(LLContext ctx);

    public static void compileList(LLContext ctx, List<LLExpression> list) {
        list.forEach(e -> e.compile(ctx));
    }

    @Override
    public abstract String toString();

}
