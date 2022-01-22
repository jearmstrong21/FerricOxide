package mackycheese21.ferricoxide.nast.ll.expr;

import mackycheese21.ferricoxide.nast.ll.LLContext;
import mackycheese21.ferricoxide.nast.ll.LLExpression;
import mackycheese21.ferricoxide.nast.ll.LLType;
import org.bytedeco.llvm.global.LLVM;

public class LLReturn extends LLExpression {

    public final LLExpression ret;

    public LLReturn(LLExpression ret) {
        this.ret = ret;
    }

    @Override
    public void compile(LLContext ctx) {
        ret.compile(ctx);
        if (ret.value.type().equals(LLType.none())) LLVM.LLVMBuildRetVoid(ctx.builder());
        else LLVM.LLVMBuildRet(ctx.builder(), ret.value.ref());
    }

    @Override
    public String toString() {
        return "return %s;".formatted(ret);
    }
}
