package mackycheese21.ferricoxide.nast.ll.expr;

import mackycheese21.ferricoxide.nast.ll.LLContext;
import mackycheese21.ferricoxide.nast.ll.LLType;
import mackycheese21.ferricoxide.nast.ll.LLValue;
import org.bytedeco.llvm.global.LLVM;

public class LLNone extends LLExpression {

    @Override
    public void compile(LLContext ctx) {
        value = new LLValue(LLType.none(), LLVM.LLVMGetUndef(LLVM.LLVMVoidType()));
    }

    @Override
    public String toString() {
        return "()";
    }
}
