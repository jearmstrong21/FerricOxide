package mackycheese21.ferricoxide.nast.ll.expr;

import mackycheese21.ferricoxide.nast.ll.LLContext;

import java.util.Objects;

public class LLAccessFunction extends LLExpression {

    public final String name;

    public LLAccessFunction(String name) {
        this.name = name;
    }

    @Override
    public void compile(LLContext ctx) {
        value = Objects.requireNonNull(ctx.functions().get(name));
    }

    @Override
    public String toString() {
        return name;
    }
}
