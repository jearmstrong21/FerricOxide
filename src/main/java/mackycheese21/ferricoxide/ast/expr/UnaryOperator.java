package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.ast.type.FOType;
import mackycheese21.ferricoxide.ast.type.PointerType;
import mackycheese21.ferricoxide.ast.type.TypeRegistry;
import mackycheese21.ferricoxide.parser.token.PunctToken;
import mackycheese21.ferricoxide.parser.token.Span;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import static org.bytedeco.llvm.global.LLVM.*;

public enum UnaryOperator {

    NEGATE(true, PunctToken.Type.MINUS),
    LOGICAL_NOT(false, PunctToken.Type.NOT),
    BITWISE_NOT(false, PunctToken.Type.TILDE),
    DEREF(false, PunctToken.Type.ASTERISK);

    public final boolean arith;
    public final PunctToken.Type punctuation;

    UnaryOperator(boolean arith, PunctToken.Type punctuation) {
        this.arith = arith;
        this.punctuation = punctuation;
    }

    public FOType validate(Span span, FOType operand) {
        switch (this) {
            case NEGATE -> {
                if (operand == FOType.I8)
                    return FOType.I8;
                if (operand == FOType.I32)
                    return FOType.I32;
                if (operand == FOType.F32)
                    return FOType.F32;
            }
            case LOGICAL_NOT -> {
                if (operand == FOType.BOOL)
                    return FOType.BOOL;
            }
            case BITWISE_NOT -> {
                if (operand == FOType.I8)
                    return FOType.I8;
            }
            case DEREF -> {
                if (operand instanceof PointerType pointer)
                    return pointer.to;
            }
        }
        throw new AnalysisException(span, "cannot apply unary operator %s to %s".formatted(this, operand));
    }

    public LLVMValueRef compile(LLVMBuilderRef builder, LLVMValueRef a, FOType operand) {
        String name = toString().toLowerCase();
        switch (this) {
            case NEGATE -> {
                if (operand == FOType.I8)
                    return LLVMBuildSub(builder, LLVMConstInt(TypeRegistry.forceLookup(operand), 0, 0), a, name);
                if (operand == FOType.I32)
                    return LLVMBuildSub(builder, LLVMConstInt(TypeRegistry.forceLookup(operand), 0, 0), a, name);
                if (operand == FOType.F32)
                    return LLVMBuildFNeg(builder, a, name);
            }
            case LOGICAL_NOT -> {
                if (operand == FOType.BOOL)
                    return LLVMBuildNot(builder, a, name);
            }
            case BITWISE_NOT -> {
                if (operand == FOType.I8)
                    return LLVMBuildNot(builder, a, name);
            }
            case DEREF -> {
                if (operand instanceof PointerType pointer)
                    return LLVMBuildLoad2(builder, TypeRegistry.forceLookup(pointer.to), a, name);
            }
        }
        throw new AssertionError();
    }

}
