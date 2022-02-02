package mackycheese21.ferricoxide.nast.ll.expr;

import mackycheese21.ferricoxide.nast.ll.LLContext;
import mackycheese21.ferricoxide.nast.ll.LLType;
import mackycheese21.ferricoxide.nast.ll.LLValue;
import org.bytedeco.llvm.global.LLVM;

public class LLZeroInit extends LLExpression {

    public final LLType type;

    public LLZeroInit(LLType type) {
        this.type = type;
    }

    @Override
    public void compile(LLContext ctx) {
        if(type.flags.contains(LLType.Flag.VOID)) value = new LLValue(type, LLVM.LLVMGetUndef(LLVM.LLVMVoidType()));
        else value = new LLValue(type, LLVM.LLVMConstNull(type.ref));
    }

    @Override
    public String toString() {
        return "zeroinit(%s)".formatted(type);
    }
}
