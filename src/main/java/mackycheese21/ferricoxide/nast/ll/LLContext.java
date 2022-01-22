package mackycheese21.ferricoxide.nast.ll;

import org.bytedeco.llvm.LLVM.LLVMBuilderRef;

import java.util.List;
import java.util.Map;

public record LLContext(LLVMBuilderRef builder,
                        List<LLValue> locals,
                        Map<String, LLValue> globals,
                        Map<String, LLValue> functions,
                        Map<String, LLType> structs) {
}
