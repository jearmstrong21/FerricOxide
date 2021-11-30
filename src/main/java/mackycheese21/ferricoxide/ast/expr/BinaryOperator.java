package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.ast.type.ConcreteType;
import mackycheese21.ferricoxide.ast.type.PointerType;
import mackycheese21.ferricoxide.parser.token.Token;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import static org.bytedeco.llvm.global.LLVM.*;

public enum BinaryOperator {
    MUL(140, true, Token.Punctuation.STAR),
    DIV(130, true, Token.Punctuation.SLASH),

    ADD(120, true, Token.Punctuation.PLUS),
    SUB(110, true, Token.Punctuation.MINUS),

    LE(100, false, Token.Punctuation.LE),
    LT(90, false, Token.Punctuation.LT),
    GE(80, false, Token.Punctuation.GE),
    GT(70, false, Token.Punctuation.GT),

    EQ(60, false, Token.Punctuation.EQEQ),
    NEQ(50, false, Token.Punctuation.NEQ),

    BITWISE_AND(40, true, Token.Punctuation.AND),
    BITWISE_XOR(30, true, Token.Punctuation.XOR),
    BITWISE_OR(20, true, Token.Punctuation.OR),

    LOGICAL_AND(10, false, Token.Punctuation.ANDAND),
    LOGICAL_OR(0, false, Token.Punctuation.OROR),

    DISCARD_FIRST(-1, false, Token.Punctuation.EQ);

    public final int priority;
    public final boolean arith;
    public final Token.Punctuation punctuation;

    BinaryOperator(int priority, boolean arith, Token.Punctuation punctuation) {
        this.priority = priority;
        this.arith = arith;
        this.punctuation = punctuation;
    }

    public ConcreteType getResult(ConcreteType operand) {
        if (this == DISCARD_FIRST) return operand;
        switch (this) {
            case MUL -> {
                if (operand == ConcreteType.I8) return ConcreteType.I8;
                if (operand == ConcreteType.I32) return ConcreteType.I32;
            }
            case DIV -> {
                if (operand == ConcreteType.I8) return ConcreteType.I8;
                if (operand == ConcreteType.I32) return ConcreteType.I32;
            }
            case ADD -> {
                if (operand == ConcreteType.I8) return ConcreteType.I8;
                if (operand == ConcreteType.I32) return ConcreteType.I32;
            }
            case SUB -> {
                if (operand == ConcreteType.I8) return ConcreteType.I8;
                if (operand == ConcreteType.I32) return ConcreteType.I32;
            }
            case LE -> {
                if (operand == ConcreteType.I8) return ConcreteType.BOOL;
                if (operand == ConcreteType.I32) return ConcreteType.BOOL;
            }
            case LT -> {
                if (operand == ConcreteType.I8) return ConcreteType.BOOL;
                if (operand == ConcreteType.I32) return ConcreteType.BOOL;
            }
            case GE -> {
                if (operand == ConcreteType.I8) return ConcreteType.BOOL;
                if (operand == ConcreteType.I32) return ConcreteType.BOOL;
            }
            case GT -> {
                if (operand == ConcreteType.I8) return ConcreteType.BOOL;
                if (operand == ConcreteType.I32) return ConcreteType.BOOL;
            }
            case EQ -> {
                if (operand == ConcreteType.I8) return ConcreteType.BOOL;
                if (operand == ConcreteType.I32) return ConcreteType.BOOL;
                if (operand instanceof PointerType) return ConcreteType.BOOL;
            }
            case NEQ -> {
                if (operand == ConcreteType.I8) return ConcreteType.BOOL;
                if (operand == ConcreteType.I32) return ConcreteType.BOOL;
                if (operand instanceof PointerType) return ConcreteType.BOOL;
            }
            case BITWISE_AND -> {
                if (operand == ConcreteType.I8) return ConcreteType.I8;
                if (operand == ConcreteType.I32) return ConcreteType.I32;
            }
            case BITWISE_XOR -> {
                if (operand == ConcreteType.I8) return ConcreteType.I8;
                if (operand == ConcreteType.I32) return ConcreteType.I32;
            }
            case BITWISE_OR -> {
                if (operand == ConcreteType.I8) return ConcreteType.I8;
                if (operand == ConcreteType.I32) return ConcreteType.I32;
            }
            case LOGICAL_AND -> {
            }
            case LOGICAL_OR -> {
            }
        }
        throw AnalysisException.cannotApplyBinaryOperator(this, operand);
    }

    public LLVMValueRef compile(LLVMBuilderRef builder, LLVMValueRef a, LLVMValueRef b, ConcreteType operand) {
        if (this == DISCARD_FIRST) return b;
        String name = "BinaryOperator." + this;
        switch (this) {
            case MUL -> {
                if (operand == ConcreteType.I8) return LLVMBuildMul(builder, a, b, name);
                if (operand == ConcreteType.I32) return LLVMBuildMul(builder, a, b, name);
            }
            case DIV -> {
                if (operand == ConcreteType.I8) return LLVMBuildSDiv(builder, a, b, name);
                if (operand == ConcreteType.I32) return LLVMBuildSDiv(builder, a, b, name);
            }
            case ADD -> {
                if (operand == ConcreteType.I8) return LLVMBuildAdd(builder, a, b, name);
                if (operand == ConcreteType.I32) return LLVMBuildAdd(builder, a, b, name);
            }
            case SUB -> {
                if (operand == ConcreteType.I8) return LLVMBuildSub(builder, a, b, name);
                if (operand == ConcreteType.I32) return LLVMBuildSub(builder, a, b, name);
            }
            case LE -> {
                if (operand == ConcreteType.I8) return LLVMBuildICmp(builder, LLVMIntSLE, a, b, name);
                if (operand == ConcreteType.I32) return LLVMBuildICmp(builder, LLVMIntSLE, a, b, name);
            }
            case LT -> {
                if (operand == ConcreteType.I8) return LLVMBuildICmp(builder, LLVMIntSLT, a, b, name);
                if (operand == ConcreteType.I32) return LLVMBuildICmp(builder, LLVMIntSLT, a, b, name);
            }
            case GE -> {
                if (operand == ConcreteType.I8) return LLVMBuildICmp(builder, LLVMIntSGE, a, b, name);
                if (operand == ConcreteType.I32) return LLVMBuildICmp(builder, LLVMIntSGE, a, b, name);
            }
            case GT -> {
                if (operand == ConcreteType.I8) return LLVMBuildICmp(builder, LLVMIntSGT, a, b, name);
                if (operand == ConcreteType.I32) return LLVMBuildICmp(builder, LLVMIntSGT, a, b, name);
            }
            case EQ -> {
                if (operand == ConcreteType.I8) return LLVMBuildICmp(builder, LLVMIntEQ, a, b, name);
                if (operand == ConcreteType.I32) return LLVMBuildICmp(builder, LLVMIntEQ, a, b, name);
                if (operand instanceof PointerType) return LLVMBuildICmp(builder, LLVMIntEQ,
                        LLVMBuildPtrToInt(builder, a, ConcreteType.I32.typeRef, name + ".CastA"),
                        LLVMBuildPtrToInt(builder, b, ConcreteType.I32.typeRef, name + ".CastB"),
                        name);
            }
            case NEQ -> {
                if (operand == ConcreteType.I8) return LLVMBuildICmp(builder, LLVMIntNE, a, b, name);
                if (operand == ConcreteType.I32) return LLVMBuildICmp(builder, LLVMIntNE, a, b, name);
                if (operand instanceof PointerType) return LLVMBuildICmp(builder, LLVMIntNE,
                        LLVMBuildPtrToInt(builder, a, ConcreteType.I32.typeRef, name + ".CastA"),
                        LLVMBuildPtrToInt(builder, b, ConcreteType.I32.typeRef, name + ".CastB"),
                        name);
            }
            case BITWISE_AND -> {
                if (operand == ConcreteType.I8) return LLVMBuildAnd(builder, a, b, name);
                if (operand == ConcreteType.I32) return LLVMBuildAnd(builder, a, b, name);
            }
            case BITWISE_XOR -> {
                if (operand == ConcreteType.I8) return LLVMBuildXor(builder, a, b, name);
                if (operand == ConcreteType.I32) return LLVMBuildXor(builder, a, b, name);
            }
            case BITWISE_OR -> {
                if (operand == ConcreteType.I8) return LLVMBuildOr(builder, a, b, name);
                if (operand == ConcreteType.I32) return LLVMBuildOr(builder, a, b, name);
            }
            case LOGICAL_AND -> {
            }
            case LOGICAL_OR -> {
            }
        }
        throw AnalysisException.cannotApplyBinaryOperator(this, operand);
    }

}
