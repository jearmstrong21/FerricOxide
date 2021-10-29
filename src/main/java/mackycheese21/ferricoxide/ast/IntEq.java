package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.ConcreteType;
import mackycheese21.ferricoxide.GlobalContext;
import mackycheese21.ferricoxide.Utils;
import mackycheese21.ferricoxide.Variables;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import static org.bytedeco.llvm.global.LLVM.*;

public class IntEq extends Ast {

    private final Ast a;
    private final Ast b;

    public IntEq(Ast a, Ast b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public ConcreteType getConcreteType(GlobalContext globalContext, Variables variables) {
        Utils.assertTrue(a.getConcreteType(globalContext, variables).equals(ConcreteType.I32));
        Utils.assertTrue(b.getConcreteType(globalContext, variables).equals(ConcreteType.I32));
        return ConcreteType.I32;
    }

    @Override
    public LLVMValueRef generateIR(GlobalContext globalContext, Variables variables, LLVMBuilderRef builder) {
        return LLVMBuildICmp(builder, LLVMIntEQ, a.generateIR(globalContext, variables, builder), b.generateIR(globalContext, variables, builder), "IntEq");
    }
}
