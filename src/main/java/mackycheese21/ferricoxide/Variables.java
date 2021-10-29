package mackycheese21.ferricoxide;

import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.List;

import static org.bytedeco.llvm.global.LLVM.*;

public class Variables extends IdentifierMap<LLVMValueRef> {


    public Variables(GlobalContext globalContext, String functionName, List<String> funcArgs) {
        super(null);
        for (int i = 0; i < funcArgs.size(); i++) {
            mapAdd(funcArgs.get(i), LLVMGetParam(globalContext.mapGet(functionName).getValueRef(), i));
        }
    }

}
