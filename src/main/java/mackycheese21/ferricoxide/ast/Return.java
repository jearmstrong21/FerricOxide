package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.ConcreteType;
import mackycheese21.ferricoxide.GlobalContext;
import mackycheese21.ferricoxide.Variables;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import static org.bytedeco.llvm.global.LLVM.LLVMBuildRet;

public class Return extends Ast {

    private final Ast ast;

    public Return(Ast ast) {
        this.ast = ast;
    }

    @Override
    public ConcreteType getConcreteType(GlobalContext globalContext, Variables variables) {
        return null;
    }

    @Override
    public LLVMValueRef generateIR(GlobalContext globalContext, Variables variables, LLVMBuilderRef builder) {
        LLVMBuildRet(builder, ast.generateIR(globalContext, variables, builder));
        return null;
    }
}
