package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.GlobalContext;
import mackycheese21.ferricoxide.Utils;
import mackycheese21.ferricoxide.Variables;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import static org.bytedeco.llvm.global.LLVM.*;

public class Add extends Ast {

    private final Ast A;
    private final Ast B;

    public Add(Ast a, Ast b) {
        super(a.getConcreteType());
        Utils.assertTrue(a.getConcreteType().equals(b.getConcreteType()));
        A = a;
        B = b;
    }

    @Override
    public LLVMValueRef generateIR(GlobalContext globalContext, Variables variables, LLVMBuilderRef builder) {
        return LLVMBuildAdd(builder, A.generateIR(globalContext, variables, builder), B.generateIR(globalContext, variables, builder), "add");
    }

    @Override
    public String toString() {
        return String.format("(%s + %s)", A, B);
    }
}
