package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.ConcreteType;
import mackycheese21.ferricoxide.GlobalContext;
import mackycheese21.ferricoxide.Variables;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import static org.bytedeco.llvm.global.LLVM.LLVMConstInt;

public class IntConstant extends Ast {

    private final int value;

    public IntConstant(int value) {
        this.value = value;
    }

    @Override
    public ConcreteType getConcreteType(GlobalContext globalContext, Variables variables) {
        return ConcreteType.I32;
    }

    @Override
    public LLVMValueRef generateIR(GlobalContext globalContext, Variables variables, LLVMBuilderRef builder) {
        return LLVMConstInt(ConcreteType.I32.llvmTypeRef(), value, 0);
    }

    @Override
    public String toString() {
        return "I32[" + value + "]";
    }
}
