package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.ast.type.ConcreteType;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;
import static org.bytedeco.llvm.global.LLVM.*;

public class CastOperator {

    public static void verify(ConcreteType from, ConcreteType to) {
        if(from == ConcreteType.I32 && to == ConcreteType.I8) return;
        throw AnalysisException.invalidCast(from, to);
    }

    public static LLVMValueRef compile(LLVMBuilderRef builder, ConcreteType from, ConcreteType to, LLVMValueRef valueRef) {
        if(from == ConcreteType.I32 && to == ConcreteType.I8) return LLVMBuildZExt(builder, valueRef, to.typeRef, "CastOperator");
        throw AnalysisException.invalidCast(from, to);
    }

}
