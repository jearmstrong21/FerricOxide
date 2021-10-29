package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.ConcreteType;
import mackycheese21.ferricoxide.GlobalContext;
import mackycheese21.ferricoxide.Variables;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

public abstract class Ast {

    private final ConcreteType concreteType;

    protected Ast(ConcreteType concreteType) {
        this.concreteType = concreteType;
    }

    public final ConcreteType getConcreteType() {
        return concreteType;
    }

    public abstract LLVMValueRef generateIR(GlobalContext globalContext, Variables variables, LLVMBuilderRef builder);

}
