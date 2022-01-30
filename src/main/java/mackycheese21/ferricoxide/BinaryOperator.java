package mackycheese21.ferricoxide;

import mackycheese21.ferricoxide.nast.hl.HLContext;
import mackycheese21.ferricoxide.nast.hl.HLValue;
import mackycheese21.ferricoxide.nast.hl.expr.HLExpression;
import mackycheese21.ferricoxide.nast.hl.type.HLPointerTypeId;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;
import mackycheese21.ferricoxide.nast.ll.LLType;
import mackycheese21.ferricoxide.nast.ll.LLValue;
import mackycheese21.ferricoxide.nast.ll.expr.LLBinary;
import mackycheese21.ferricoxide.parser.token.PunctToken;
import mackycheese21.ferricoxide.parser.token.Span;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;
import org.bytedeco.llvm.global.LLVM;

public enum BinaryOperator {
    MUL(140, Category.ARITH, PunctToken.Type.ASTERISK),
    DIV(130, Category.ARITH, PunctToken.Type.SLASH),
    MOD(125, Category.ARITH, PunctToken.Type.PERCENT),

    ADD(120, Category.ARITH, PunctToken.Type.PLUS),
    SUB(110, Category.ARITH, PunctToken.Type.MINUS),

    LE(100, Category.COMPARE, PunctToken.Type.LT_EQ),
    LT(90, Category.COMPARE, PunctToken.Type.LT),
    GE(80, Category.COMPARE, PunctToken.Type.GT_EQ),
    GT(70, Category.COMPARE, PunctToken.Type.GT),

    EQ(60, Category.ANY, PunctToken.Type.EQEQ),
    NEQ(50, Category.ANY, PunctToken.Type.NOTEQ),

    AND(40, Category.INTEGER, PunctToken.Type.AND),
    XOR(30, Category.INTEGER, PunctToken.Type.XOR),
    OR(20, Category.INTEGER, PunctToken.Type.OR),

    ASSIGN(-1, Category.ANY, PunctToken.Type.EQ),
    DISCARD_FIRST(-1, Category.ARITH, PunctToken.Type.EQ);

    public enum Category {
        INTEGER,
        ARITH,
        ANY,
        COMPARE
    }

    public final int priority;
    public final Category category;
    public final PunctToken.Type punctuation;

    BinaryOperator(int priority, Category category, PunctToken.Type punctuation) {
        this.priority = priority;
        this.category = category;
        this.punctuation = punctuation;
    }

    private boolean applies(HLTypeId a) {
        return switch (category) {
            case INTEGER -> a.integerType;
            case ARITH, COMPARE -> a.integerType || a.floatType;
            case ANY -> a instanceof HLPointerTypeId || a.integerType || a.floatType;
        };
    }

    public HLValue run(HLContext ctx, HLExpression a, HLExpression b) {
        HLTypeId result;
        if (this == ASSIGN) {
            a.require(ctx, new HLPointerTypeId(Span.NONE, b.value.type()).pred());// 'a' must be a pointer to 'b'
            result = HLTypeId.none(Span.NONE);
        } else {
            b.require(ctx, a.value.type().pred());
            if (!applies(a.value.type()))
                throw new AnalysisException(a.span, "operator %s does not apply to %s".formatted(this, a));
            result = a.value.type();
        }
        return new HLValue(result, new LLBinary(this, a.value.ll(), b.value.ll()));
    }

    private interface BinOp {
        LLVMValueRef apply(LLVMBuilderRef builder, LLVMValueRef a, LLVMValueRef b, String name);
    }

    // pointers go through i as well
    private LLValue run(LLVMBuilderRef builder, LLValue a, LLValue b, BinOp si, BinOp ui, BinOp f) {
        if (a.type().floatType) return new LLValue(a.type(), f.apply(builder, a.ref(), b.ref(), toString()));
        if (a.type().signedInteger) return new LLValue(a.type(), si.apply(builder, a.ref(), b.ref(), toString()));
        return new LLValue(a.type(), ui.apply(builder, a.ref(), b.ref(), toString()));
    }

    private LLValue run(LLVMBuilderRef builder, LLValue a, LLValue b, int si, int ui, int f) {
        if (a.type().floatType)
            return new LLValue(a.type(), LLVM.LLVMBuildFCmp(builder, f, a.ref(), b.ref(), toString()));
        if (a.type().signedInteger)
            return new LLValue(a.type(), LLVM.LLVMBuildICmp(builder, si, a.ref(), b.ref(), toString()));
        return new LLValue(a.type(), LLVM.LLVMBuildFCmp(builder, f, a.ref(), b.ref(), toString()));
    }

    public LLValue run(LLVMBuilderRef builder, LLValue a, LLValue b) {
        return switch (this) {
            case EQ -> run(builder, a, b, LLVM.LLVMIntEQ, LLVM.LLVMIntEQ, LLVM.LLVMRealOEQ);
            case GE -> run(builder, a, b, LLVM.LLVMIntSGE, LLVM.LLVMIntUGE, LLVM.LLVMRealOGE);
            case GT -> run(builder, a, b, LLVM.LLVMIntSGT, LLVM.LLVMIntUGT, LLVM.LLVMRealOGT);
            case LE -> run(builder, a, b, LLVM.LLVMIntSLE, LLVM.LLVMIntULE, LLVM.LLVMRealOLE);
            case LT -> run(builder, a, b, LLVM.LLVMIntSLT, LLVM.LLVMIntULT, LLVM.LLVMRealOLT);
            case OR -> run(builder, a, b, LLVM::LLVMBuildOr, LLVM::LLVMBuildOr, null);
            case ADD -> run(builder, a, b, LLVM::LLVMBuildAdd, LLVM::LLVMBuildAdd, LLVM::LLVMBuildFAdd);
            case AND -> run(builder, a, b, LLVM::LLVMBuildAnd, LLVM::LLVMBuildAnd, null);
            case DIV -> run(builder, a, b, LLVM::LLVMBuildSDiv, LLVM::LLVMBuildUDiv, LLVM::LLVMBuildFDiv);
            case MOD -> run(builder, a, b, LLVM::LLVMBuildSRem, LLVM::LLVMBuildURem, LLVM::LLVMBuildFRem);
            case MUL -> run(builder, a, b, LLVM::LLVMBuildMul, LLVM::LLVMBuildMul, LLVM::LLVMBuildFMul);
            case NEQ -> run(builder, a, b, LLVM.LLVMIntNE, LLVM.LLVMIntNE, LLVM.LLVMRealONE);
            case SUB -> run(builder, a, b, LLVM::LLVMBuildSub, LLVM::LLVMBuildSub, LLVM::LLVMBuildFSub);
            case XOR -> run(builder, a, b, LLVM::LLVMBuildXor, LLVM::LLVMBuildXor, null);
            case ASSIGN -> new LLValue(LLType.none(), LLVM.LLVMBuildStore(builder, b.ref(), a.ref()));
            case DISCARD_FIRST -> b;
        };
    }

//
//    public LLVMValueRef compile(LLVMBuilderRef builder, LLVMValueRef a, LLVMValueRef b, FOType operand) {
//        if (this == DISCARD_FIRST) return b;
//        String name = toString().toLowerCase();
//        switch (this) {
//            case MUL -> {
//                if (operand == FOType.I8) return LLVMBuildMul(builder, a, b, name);
//                if (operand == FOType.I32) return LLVMBuildMul(builder, a, b, name);
//                if (operand == FOType.F32) return LLVMBuildFMul(builder, a, b, name);
//            }
//            case DIV -> {
//                if (operand == FOType.I8) return LLVMBuildSDiv(builder, a, b, name);
//                if (operand == FOType.I32) return LLVMBuildSDiv(builder, a, b, name);
//                if (operand == FOType.F32) return LLVMBuildFDiv(builder, a, b, name);
//            }
//            case MOD -> {
//                if (operand == FOType.I8) return LLVMBuildSRem(builder, a, b, name);
//                if (operand == FOType.I32) return LLVMBuildSRem(builder, a, b, name);
//            }
//            case ADD -> {
//                if (operand == FOType.I8) return LLVMBuildAdd(builder, a, b, name);
//                if (operand == FOType.I32) return LLVMBuildAdd(builder, a, b, name);
//                if (operand == FOType.F32) return LLVMBuildFAdd(builder, a, b, name);
//            }
//            case SUB -> {
//                if (operand == FOType.I8) return LLVMBuildSub(builder, a, b, name);
//                if (operand == FOType.I32) return LLVMBuildSub(builder, a, b, name);
//                if (operand == FOType.F32) return LLVMBuildFSub(builder, a, b, name);
//            }
//            case LE -> {
//                if (operand == FOType.I8) return LLVMBuildICmp(builder, LLVMIntSLE, a, b, name);
//                if (operand == FOType.I32) return LLVMBuildICmp(builder, LLVMIntSLE, a, b, name);
//                if (operand == FOType.F32) return LLVMBuildFCmp(builder, LLVMRealOLE, a, b, name);
//            }
//            case LT -> {
//                if (operand == FOType.I8) return LLVMBuildICmp(builder, LLVMIntSLT, a, b, name);
//                if (operand == FOType.I32) return LLVMBuildICmp(builder, LLVMIntSLT, a, b, name);
//                if (operand == FOType.F32) return LLVMBuildFCmp(builder, LLVMRealOLT, a, b, name);
//            }
//            case GE -> {
//                if (operand == FOType.I8) return LLVMBuildICmp(builder, LLVMIntSGE, a, b, name);
//                if (operand == FOType.I32) return LLVMBuildICmp(builder, LLVMIntSGE, a, b, name);
//                if (operand == FOType.F32) return LLVMBuildFCmp(builder, LLVMRealOGE, a, b, name);
//            }
//            case GT -> {
//                if (operand == FOType.I8) return LLVMBuildICmp(builder, LLVMIntSGT, a, b, name);
//                if (operand == FOType.I32) return LLVMBuildICmp(builder, LLVMIntSGT, a, b, name);
//                if (operand == FOType.F32) return LLVMBuildFCmp(builder, LLVMRealOGT, a, b, name);
//            }
//            case EQ -> {
//                if (operand == FOType.I8) return LLVMBuildICmp(builder, LLVMIntEQ, a, b, name);
//                if (operand == FOType.I32) return LLVMBuildICmp(builder, LLVMIntEQ, a, b, name);
//                if (operand instanceof PointerType) return LLVMBuildICmp(builder, LLVMIntEQ,
//                        LLVMBuildPtrToInt(builder, a, TypeRegistry.forceLookup(FOType.I32), name + ".CastA"),
//                        LLVMBuildPtrToInt(builder, b, TypeRegistry.forceLookup(FOType.I32), name + ".CastB"),
//                        name);
//                if (operand == FOType.F32) return LLVMBuildFCmp(builder, LLVMRealOEQ, a, b, name);
//            }
//            case NEQ -> {
//                if (operand == FOType.I8) return LLVMBuildICmp(builder, LLVMIntNE, a, b, name);
//                if (operand == FOType.I32) return LLVMBuildICmp(builder, LLVMIntNE, a, b, name);
//                if (operand instanceof PointerType) return LLVMBuildICmp(builder, LLVMIntNE,
//                        LLVMBuildPtrToInt(builder, a, TypeRegistry.forceLookup(FOType.I32), name + ".CastA"),
//                        LLVMBuildPtrToInt(builder, b, TypeRegistry.forceLookup(FOType.I32), name + ".CastB"),
//                        name);
//                if (operand == FOType.F32) return LLVMBuildFCmp(builder, LLVMRealONE, a, b, name);
//            }
//            case BITWISE_AND -> {
//                if (operand == FOType.I8) return LLVMBuildAnd(builder, a, b, name);
//                if (operand == FOType.I32) return LLVMBuildAnd(builder, a, b, name);
//            }
//            case BITWISE_XOR -> {
//                if (operand == FOType.I8) return LLVMBuildXor(builder, a, b, name);
//                if (operand == FOType.I32) return LLVMBuildXor(builder, a, b, name);
//            }
//            case BITWISE_OR -> {
//                if (operand == FOType.I8) return LLVMBuildOr(builder, a, b, name);
//                if (operand == FOType.I32) return LLVMBuildOr(builder, a, b, name);
//            }
//            case LOGICAL_AND -> {
//                if (operand == FOType.BOOL) return LLVMBuildAnd(builder, a, b, name);
//            }
//            case LOGICAL_OR -> {
//                if (operand == FOType.BOOL) return LLVMBuildOr(builder, a, b, name);
//            }
//        }
//        throw new AssertionError();
//    }

}