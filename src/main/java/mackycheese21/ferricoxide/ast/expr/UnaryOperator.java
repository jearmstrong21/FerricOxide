package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.ast.type.ConcreteType;
import mackycheese21.ferricoxide.ast.type.PointerType;
import mackycheese21.ferricoxide.parser.token.Token;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import static org.bytedeco.llvm.global.LLVM.*;

public enum UnaryOperator {

    NEGATE(true, Token.Punctuation.MINUS),
    LOGICAL_NOT(false, Token.Punctuation.BANG),
    BITWISE_NOT(false, Token.Punctuation.TILDE),
    DEREF(false, Token.Punctuation.STAR);

    public final boolean arith;
    public final Token.Punctuation punctuation;

    UnaryOperator(boolean arith, Token.Punctuation punctuation) {
        this.arith = arith;
        this.punctuation = punctuation;
    }

    public ConcreteType getResult(ConcreteType operand) {
        switch (this) {
            case NEGATE -> {
                if (operand == ConcreteType.I32)
                    return ConcreteType.I32;
                if (operand == ConcreteType.F32)
                    return ConcreteType.F32;
            }
            case LOGICAL_NOT -> {
                if (operand == ConcreteType.BOOL)
                    return ConcreteType.BOOL;
            }
            case BITWISE_NOT -> {
            }
            case DEREF -> {
                if (operand instanceof PointerType pointer)
                    return pointer.to;
            }
        }
        throw AnalysisException.cannotApplyUnaryOperator(this, operand);
    }

    public LLVMValueRef compile(LLVMBuilderRef builder, LLVMValueRef a, ConcreteType operand) {
        String name = "UnaryOperator." + this;
        switch (this) {
            case NEGATE -> {
                if (operand == ConcreteType.I32)
                    return LLVMBuildSub(builder, LLVMConstInt(operand.typeRef, 0, 0), a, name);
                if (operand == ConcreteType.F32)
                    return LLVMBuildFNeg(builder, a, name);
            }
            case LOGICAL_NOT -> {
                if (operand == ConcreteType.BOOL)
                    return LLVMBuildNot(builder, a, name);
            }
            case BITWISE_NOT -> {
            }
            case DEREF -> {
                if (operand instanceof PointerType pointer)
                    return LLVMBuildLoad2(builder, pointer.to.typeRef, a, name);
            }
        }
        throw AnalysisException.cannotApplyUnaryOperator(this, operand);
    }

}
