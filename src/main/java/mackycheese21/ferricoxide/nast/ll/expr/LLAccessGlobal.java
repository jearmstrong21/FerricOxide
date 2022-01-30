package mackycheese21.ferricoxide.nast.ll.expr;

import mackycheese21.ferricoxide.nast.ll.LLContext;

public class LLAccessGlobal extends LLExpression {

    public final String name;

    public LLAccessGlobal(String name) {
        this.name = name;
    }

    @Override
    public void compile(LLContext ctx) {
        value = ctx.globals().get(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
