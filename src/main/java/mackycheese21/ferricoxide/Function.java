package mackycheese21.ferricoxide;

import mackycheese21.ferricoxide.ast.Ast;
import mackycheese21.ferricoxide.ast.DeclareVar;
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
        LLVMBasicBlockRef entry = LLVMAppendBasicBlock(valueRef, "entry");
        LLVMPositionBuilderAtEnd(builder, entry);
        Variables variables = new Variables(this);
        Utils.assertTrue(paramNames.size() == params.size());
        for (int i = 0; i < paramNames.size(); i++) {
            int finalI = i;
            new DeclareVar(paramNames.get(i), new Ast() {
                @Override
                public ConcreteType getConcreteType(GlobalContext globalContext, Variables variables) {
                    return params.get(finalI);
                }

                @Override
                public LLVMValueRef generateIR(GlobalContext globalContext, Variables variables, LLVMBuilderRef builder) {
                    return LLVMGetParam(valueRef, finalI);
                }
            }).generateIR(null, variables, builder);
        }
        return variables;
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
