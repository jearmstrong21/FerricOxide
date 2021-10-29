package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.ConcreteType;
import mackycheese21.ferricoxide.GlobalContext;
import mackycheese21.ferricoxide.Variables;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

public abstract class Ast {

    public abstract ConcreteType getConcreteType(GlobalContext globalContext, Variables variables);

    public abstract LLVMValueRef generateIR(GlobalContext globalContext, Variables variables, LLVMBuilderRef builder);

}
