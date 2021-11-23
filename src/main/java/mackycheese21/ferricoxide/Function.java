package mackycheese21.ferricoxide;

import mackycheese21.ferricoxide.ast.Ast;
import mackycheese21.ferricoxide.ast.DeclareVar;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;

import java.util.Collections;
import java.util.List;

import static org.bytedeco.llvm.global.LLVM.*;

public class Function {

    public final String name;
    public final ConcreteType result;
    public final List<ConcreteType> params;
    public final LLVMTypeRef typeRef;
    public final LLVMValueRef valueRef;
    public final boolean extern;

    public Function(String name, ConcreteType result, List<ConcreteType> params, LLVMModuleRef module, boolean extern) {
        this.name = name;
        this.result = result;
        this.params = Collections.unmodifiableList(params);
        PointerPointer<LLVMTypeRef> paramType = new PointerPointer<>(params.size());
        paramType.put(params.stream().map(ConcreteType::llvmTypeRef).toArray(LLVMTypeRef[]::new));
        typeRef = LLVMFunctionType(result.llvmTypeRef(), paramType, params.size(), 0);
        this.valueRef = LLVMAddFunction(module, name, typeRef);
        this.extern = extern;
        LLVMSetFunctionCallConv(valueRef, LLVMCCallConv);
    }

    public Variables enter(LLVMBuilderRef builder, List<String> paramNames) {
        if(extern) throw new UnsupportedOperationException();
        LLVMBasicBlockRef entry = LLVMAppendBasicBlock(valueRef, "entry");
        LLVMPositionBuilderAtEnd(builder, entry);
        Variables variables = new Variables(this);
        Utils.assertTrue(paramNames.size() == params.size());
        for (int i = 0; i < paramNames.size(); i++) {
            int finalI = i;
            new DeclareVar(params.get(i), paramNames.get(i), new Ast() {
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

}
