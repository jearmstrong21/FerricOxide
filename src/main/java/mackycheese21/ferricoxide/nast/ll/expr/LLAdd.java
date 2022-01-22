package mackycheese21.ferricoxide.nast.ll.expr;

import mackycheese21.ferricoxide.nast.ll.LLContext;
import mackycheese21.ferricoxide.nast.ll.LLExpression;
import mackycheese21.ferricoxide.nast.ll.LLType;
import mackycheese21.ferricoxide.nast.ll.LLValue;
import org.bytedeco.llvm.global.LLVM;

public class LLAdd extends LLExpression {

    public final LLExpression a, b;

    public LLAdd(LLExpression a, LLExpression b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public void compile(LLContext ctx) {
        a.compile(ctx);
        b.compile(ctx);
        value = new LLValue(LLType.i32(), LLVM.LLVMBuildAdd(ctx.builder(), a.value.ref(), b.value.ref(), "add"));
    }

    @Override
    public String toString() {
        return "%s + %s".formatted(a, b);
    }
}
