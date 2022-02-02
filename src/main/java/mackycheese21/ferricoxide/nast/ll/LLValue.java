package mackycheese21.ferricoxide.nast.ll;

import org.bytedeco.llvm.LLVM.LLVMValueRef;
import org.bytedeco.llvm.global.LLVM;

public record LLValue(LLType type, LLVMValueRef ref) {

    public static LLValue none() {
        return new LLValue(LLType.none(), LLVM.LLVMGetUndef(LLVM.LLVMVoidType()));
    }

}
