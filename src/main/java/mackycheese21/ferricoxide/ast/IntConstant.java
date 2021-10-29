package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.ConcreteType;
import mackycheese21.ferricoxide.GlobalContext;
import mackycheese21.ferricoxide.Variables;
import mackycheese21.ferricoxide.ast.Ast;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import static org.bytedeco.llvm.global.LLVM.*;

public class IntConstant extends Ast {

    private final int value;

    public IntConstant(int value) {
        super(ConcreteType.I32);
        this.value = value;
    }

    @Override
    public LLVMValueRef generateIR(GlobalContext globalContext, Variables variables, LLVMBuilderRef builder) {
        return LLVMConstInt(getConcreteType().llvmTypeRef(), value, 0);
    }

    @Override
    public String toString() {
        return "I32[" + value + "]";
    }
}
