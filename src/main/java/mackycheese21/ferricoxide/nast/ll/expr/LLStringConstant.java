package mackycheese21.ferricoxide.nast.ll.expr;

import mackycheese21.ferricoxide.nast.hl.expr.HLStringConstant;
import mackycheese21.ferricoxide.nast.ll.LLContext;
import mackycheese21.ferricoxide.nast.ll.LLType;
import mackycheese21.ferricoxide.nast.ll.LLValue;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMValueRef;
import org.bytedeco.llvm.global.LLVM;

public class LLStringConstant extends LLExpression {

    public final String constant;

    public LLStringConstant(String constant) {
        this.constant = constant;
    }

    @Override
    public void compile(LLContext ctx) {
        value = new LLValue(LLType.pointer(LLType.u8()), LLVM.LLVMBuildInBoundsGEP2(ctx.builder(),
                LLVM.LLVMArrayType(LLVM.LLVMIntType(8), constant.length() + 1),
                LLVM.LLVMBuildGlobalString(ctx.builder(), constant, "str"),
                new PointerPointer<>(2).put(new LLVMValueRef[]{
                        LLVM.LLVMConstInt(LLVM.LLVMIntType(32), 0, 0),
                        LLVM.LLVMConstInt(LLVM.LLVMIntType(32), 0, 0)
                }),
                2, "gep"));
    }

    @Override
    public String toString() {
        return "\"%s\"".formatted(HLStringConstant.escape(constant));
    }
}
