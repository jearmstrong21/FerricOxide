package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.type.FOType;
import mackycheese21.ferricoxide.ast.type.TypeRegistry;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import static org.bytedeco.llvm.global.LLVM.*;

//@FunctionalInterface
public class CastOperator {

    public static boolean validateImplicit(FOType from, FOType to) {
        if (from.equals(to)) return true;
        if ((from.integerType && to.integerType) && from.integerWidth < to.integerWidth) return true;
        if ((from.floatType && to.floatType) && from.floatWidth < to.floatWidth) return true;
        if (from.equals(FOType.I32) && to.pointerType) return true;
        if (from.pointerType && to.equals(FOType.I32)) return true;
        if (from.pointerType && to.pointerType) return true;
        return false;
    }

    public static boolean validate(FOType from, FOType to) {
        if (validateImplicit(from, to)) return true;
        if ((from.integerType || from.floatType) && (to.integerType || to.floatType)) return true;
        return false;
    }

    public static LLVMValueRef compile(LLVMBuilderRef builder, FOType from, FOType to, LLVMValueRef valueRef) {
        if (from == to) return valueRef;
        LLVMTypeRef toRef = TypeRegistry.forceLookup(to);
        if (from.integerType && to.integerType) {
            if (from.integerWidth < to.integerWidth) return LLVMBuildSExt(builder, valueRef, toRef, "cast");
            else return LLVMBuildTrunc(builder, valueRef, toRef, "cast");
        }
        if (from.integerType && to.floatType) {
            return LLVMBuildSIToFP(builder, valueRef, toRef, "cast");
        }
        if (from.floatType && to.integerType) {
            return LLVMBuildFPToSI(builder, valueRef, toRef, "cast");
        }
        if (from.floatType && to.floatType) {
            if (from.floatWidth < to.floatWidth) return LLVMBuildFPExt(builder, valueRef, toRef, "cast");
            else return LLVMBuildFPTrunc(builder, valueRef, toRef, "cast");
        }

        if (from == FOType.I32 && to.pointerType) {
            return LLVMBuildIntToPtr(builder, valueRef, toRef, "cast");
        }

        if (from.pointerType && to == FOType.I32) {
            return LLVMBuildPtrToInt(builder, valueRef, toRef, "cast");
        }

        if (from.pointerType && to.pointerType) {
            return LLVMBuildBitCast(builder, valueRef, toRef, "cast");
        }

        throw new UnsupportedOperationException("%s -> %s".formatted(from, to));
    }

}
