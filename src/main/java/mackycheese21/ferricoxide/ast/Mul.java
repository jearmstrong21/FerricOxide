package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.GlobalContext;
import mackycheese21.ferricoxide.Utils;
import mackycheese21.ferricoxide.Variables;
import mackycheese21.ferricoxide.ast.Ast;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import static org.bytedeco.llvm.global.LLVM.*;

public class Mul extends Ast {

    private final Ast A;
    private final Ast B;

    public Mul(Ast a, Ast b) {
        super(a.getConcreteType());
        Utils.assertTrue(a.getConcreteType().equals(b.getConcreteType()));
        A = a;
        B = b;
    }

    @Override
    public LLVMValueRef generateIR(GlobalContext globalContext, Variables variables, LLVMBuilderRef builder) {
        return LLVMBuildMul(builder, A.generateIR(globalContext, variables, builder), B.generateIR(globalContext, variables, builder), "add");
    }

    @Override
    public String toString() {
        return String.format("(%s * %s)", A, B);
    }
}
