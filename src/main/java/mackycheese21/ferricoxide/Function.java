package mackycheese21.ferricoxide;

import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;

import java.util.Collections;
import java.util.List;

import static org.bytedeco.llvm.global.LLVM.*;

public class Function {

    private final String name;
    private final ConcreteType result;
    private final List<ConcreteType> params;
    private final LLVMTypeRef typeRef;
    private final LLVMValueRef valueRef;

    public Function(String name, ConcreteType result, List<ConcreteType> params, LLVMModuleRef module) {
        this.name = name;
        this.result = result;
        this.params = Collections.unmodifiableList(params);
        PointerPointer<LLVMTypeRef> paramType = new PointerPointer<>(params.size());
        paramType.put(params.stream().map(ConcreteType::llvmTypeRef).toArray(LLVMTypeRef[]::new));
        typeRef = LLVMFunctionType(result.llvmTypeRef(), paramType, 1, 0);
        this.valueRef = LLVMAddFunction(module, name, typeRef);
        LLVMSetFunctionCallConv(valueRef, LLVMCCallConv);
    }

    public String getName() {
        return name;
    }

    public LLVMTypeRef getTypeRef() {
        return typeRef;
    }

    public Variables enter(LLVMBuilderRef builder, List<String> paramNames) {
        Variables vars = new Variables();
        Utils.assertTrue(paramNames.size() == params.size());
        for (int i = 0; i < paramNames.size(); i++) {
            LLVMValueRef valueRef = LLVMBuildAlloca(builder, params.get(i).llvmTypeRef(), "alloca");
            vars.mapAdd(paramNames.get(i), new Variables.Entry(
                    valueRef,
                    params.get(i)
            ));
            LLVMBuildStore(builder, LLVMGetParam(getValueRef(), i), valueRef);
        }
        return vars;
    }

    public ConcreteType getResult() {
        return result;
    }

    public List<ConcreteType> getParams() {
        return params;
    }

    public LLVMValueRef getValueRef() {
        return valueRef;
    }
}
