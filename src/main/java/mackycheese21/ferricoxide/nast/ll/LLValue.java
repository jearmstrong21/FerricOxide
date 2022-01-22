package mackycheese21.ferricoxide.nast.ll;

import org.bytedeco.llvm.LLVM.LLVMValueRef;

public record LLValue(LLType type, LLVMValueRef ref) {
}
