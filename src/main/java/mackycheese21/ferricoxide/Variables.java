package mackycheese21.ferricoxide;

import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.List;

import static org.bytedeco.llvm.global.LLVM.*;

public class Variables extends IdentifierMap<Variables.Entry> {

    public static final class Entry {
        public final LLVMValueRef valueRef;
        public final ConcreteType type;

        public Entry(LLVMValueRef valueRef, ConcreteType type) {
            this.valueRef = valueRef;
            this.type = type;
        }
    }

    public Variables() {
        super(null);
    }

//    public Variables(GlobalContext globalContext, Function function, List<String> funcArgs) {
//        super(null);
//        for (int i = 0; i < funcArgs.size(); i++) {
//            mapAdd(funcArgs.get(i), new Entry(
//                    LLVMGetParam(function.getValueRef(), i),
//                    function.getParams().get(i)
//            ));
//        }
//    }

//    public void mutableVariable(String name, LLVMValueRef from) {
//        LLVMBuildAlloca()
//    }

}
