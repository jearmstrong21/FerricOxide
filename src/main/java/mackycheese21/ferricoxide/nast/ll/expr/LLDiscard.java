package mackycheese21.ferricoxide.nast.ll.expr;

import mackycheese21.ferricoxide.nast.ll.LLContext;
import mackycheese21.ferricoxide.nast.ll.LLType;
import mackycheese21.ferricoxide.nast.ll.LLValue;
import org.bytedeco.llvm.global.LLVM;

public class LLDiscard extends LLExpression {

    public final LLExpression discard;

    public LLDiscard(LLExpression discard) {
        this.discard = discard;
    }

    @Override
    public void compile(LLContext ctx) {
        discard.compile(ctx);
        value = new LLValue(LLType.none(), LLVM.LLVMGetUndef(LLVM.LLVMVoidType()));
    }

    @Override
    public String toString() {
        return discard.toString() + ";";
    }
}
