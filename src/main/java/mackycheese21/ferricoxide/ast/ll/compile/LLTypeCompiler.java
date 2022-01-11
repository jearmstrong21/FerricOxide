package mackycheese21.ferricoxide.ast.ll.compile;

import mackycheese21.ferricoxide.ast.ll.type.*;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;

import java.util.Map;
import java.util.Objects;

import static org.bytedeco.llvm.global.LLVM.*;

public class LLTypeCompiler implements LLTypeVisitor<LLVMTypeRef> {

    private final Map<LLStructType, LLVMTypeRef> structs;

    public LLTypeCompiler(Map<LLStructType, LLVMTypeRef> structs) {
        this.structs = structs;
    }

    @Override
    public LLVMTypeRef visitFunction(LLFunctionType type) {
        return LLVMFunctionType(type.result.visit(this), new PointerPointer<>(type.params.size())
                .put(type.params.stream().map(t -> t.visit(this)).toArray(LLVMTypeRef[]::new)), type.params.size(), 0);
    }

    @Override
    public LLVMTypeRef visitPointer(LLPointerType type) {
        return LLVMPointerType(type.to.visit(this), 0);
    }

    @Override
    public LLVMTypeRef visitPrimitive(LLPrimitiveType type) {
        if (type == LLPrimitiveType.VOID) return LLVMVoidType();
        if (type == LLPrimitiveType.BOOL) return LLVMInt1Type();
        if (type == LLPrimitiveType.I8) return LLVMInt8Type();
        if (type == LLPrimitiveType.I16) return LLVMInt16Type();
        if (type == LLPrimitiveType.I32) return LLVMInt32Type();
        if (type == LLPrimitiveType.I64) return LLVMInt64Type();
        if (type == LLPrimitiveType.F32) return LLVMFloatType();
        if (type == LLPrimitiveType.F64) return LLVMDoubleType();
        throw new UnsupportedOperationException(type.name);
    }

    @Override
    public LLVMTypeRef visitStruct(LLStructType type) {
        return Objects.requireNonNull(structs.get(type));
    }
}
