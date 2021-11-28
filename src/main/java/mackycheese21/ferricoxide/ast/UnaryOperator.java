package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.ast.ConcreteType;
import mackycheese21.ferricoxide.parser.token.Token;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import static org.bytedeco.llvm.global.LLVM.LLVMBuildSub;
import static org.bytedeco.llvm.global.LLVM.LLVMConstInt;

public enum UnaryOperator {

    NEGATE(true, Token.Punctuation.MINUS),
    LOGICAL_NOT(false, Token.Punctuation.BANG),
    BITWISE_NOT(false, Token.Punctuation.TILDE);

    public final boolean arith;
    public final Token.Punctuation punctuation;

    UnaryOperator(boolean arith, Token.Punctuation punctuation) {
        this.arith = arith;
        this.punctuation = punctuation;
    }

    public ConcreteType getResult(ConcreteType operand) {
        switch (this) {
            case NEGATE -> {
                if (operand == ConcreteType.I32) return ConcreteType.I32;
            }
            case LOGICAL_NOT -> {
            }
            case BITWISE_NOT -> {
            }
        }
        throw AnalysisException.cannotApplyUnaryOperator(this, operand);
    }

    public LLVMValueRef compile(LLVMBuilderRef builder, LLVMValueRef a, ConcreteType operand) {
        String name = "UnaryOperator." + this;
        switch (this) {
            case NEGATE -> {
                if (operand == ConcreteType.I32)
                    return LLVMBuildSub(builder, LLVMConstInt(operand.llvmTypeRef(), 0, 0), a, name);
            }
            case LOGICAL_NOT -> {
            }
            case BITWISE_NOT -> {
            }
        }
        throw AnalysisException.cannotApplyUnaryOperator(this, operand);
    }

}
