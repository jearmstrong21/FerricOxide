package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.ast.type.ConcreteType;
import mackycheese21.ferricoxide.ast.type.PointerType;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import static org.bytedeco.llvm.global.LLVM.*;

public class CastOperator {

    public static void verify(ConcreteType from, ConcreteType to) {
        if (from == ConcreteType.I32 && to instanceof PointerType) return;
        if (from instanceof PointerType && to instanceof PointerType) return;
        if (from instanceof PointerType && to == ConcreteType.I32) return;
        if (from == ConcreteType.I32 && to == ConcreteType.I8) return;
        if (from == ConcreteType.I8 && to == ConcreteType.I32) return;
        throw AnalysisException.invalidCast(from, to);
    }

    public static LLVMValueRef compile(LLVMBuilderRef builder, ConcreteType from, ConcreteType to, LLVMValueRef valueRef) {
        String name = "CastOperator.%s.%s".formatted(from, to);
        if (from == ConcreteType.I32 && to instanceof PointerType)
            return LLVMBuildIntToPtr(builder, valueRef, to.typeRef, name);
        if (from instanceof PointerType && to instanceof PointerType)
            return LLVMBuildBitCast(builder, valueRef, to.typeRef, name);
        if (from instanceof PointerType && to == ConcreteType.I32)
            return LLVMBuildPtrToInt(builder, valueRef, to.typeRef, name);
        if (from == ConcreteType.I32 && to == ConcreteType.I8)
            return LLVMBuildZExt(builder, valueRef, to.typeRef, name);
        if (from == ConcreteType.I8 && to == ConcreteType.I32)
            return LLVMBuildSExt(builder, valueRef, to.typeRef, name);
        throw AnalysisException.invalidCast(from, to);
    }

}
