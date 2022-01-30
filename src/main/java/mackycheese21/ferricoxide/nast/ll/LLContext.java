package mackycheese21.ferricoxide.nast.ll;

import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.List;
import java.util.Map;
import java.util.Stack;

public record LLContext(LLVMBuilderRef builder,
                        LLVMValueRef currentFunction,
                        List<LLValue> locals,
                        Map<String, LLValue> globals,
                        Map<String, LLValue> functions,
                        Map<String, LLType> structs,
                        Stack<LLVMBasicBlockRef> loopBreakTargets) {
}
