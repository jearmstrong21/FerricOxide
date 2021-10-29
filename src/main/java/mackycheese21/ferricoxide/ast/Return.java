package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.GlobalContext;
import mackycheese21.ferricoxide.Variables;
import mackycheese21.ferricoxide.ast.Ast;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import static org.bytedeco.llvm.global.LLVM.*;

public class Return extends Ast {

    private final Ast ast;

    protected Return(Ast ast) {
        super(null);
        this.ast = ast;
    }

    @Override
    public LLVMValueRef generateIR(GlobalContext globalContext, Variables variables, LLVMBuilderRef builder) {
        LLVMBuildRet(builder, ast.generateIR(globalContext, variables, builder));
        return null;
    }
}
