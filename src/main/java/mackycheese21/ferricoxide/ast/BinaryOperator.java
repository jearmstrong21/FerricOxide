package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.parser.token.PunctToken;

public enum BinaryOperator {
    MUL(140, true, PunctToken.Type.ASTERISK),
    DIV(130, true, PunctToken.Type.SLASH),
    MOD(125, true, PunctToken.Type.PERCENT),

    ADD(120, true, PunctToken.Type.PLUS),
    SUB(110, true, PunctToken.Type.MINUS),

    LE(100, false, PunctToken.Type.LT_EQ),
    LT(90, false, PunctToken.Type.LT),
    GE(80, false, PunctToken.Type.GT_EQ),
    GT(70, false, PunctToken.Type.GT),

    EQ(60, false, PunctToken.Type.EQEQ),
    NEQ(50, false, PunctToken.Type.NOTEQ),

    BITWISE_AND(40, true, PunctToken.Type.AND),
    BITWISE_XOR(30, true, PunctToken.Type.XOR),
    BITWISE_OR(20, true, PunctToken.Type.OR),

    LOGICAL_AND(10, false, PunctToken.Type.ANDAND),
    LOGICAL_OR(0, false, PunctToken.Type.OROR),

    DISCARD_FIRST(-1, false, PunctToken.Type.EQ);

    public final int priority;
    public final boolean arith;
    public final PunctToken.Type punctType;

    BinaryOperator(int priority, boolean arith, PunctToken.Type punctType) {
        this.priority = priority;
        this.arith = arith;
        this.punctType = punctType;
    }

//    public FOType validate(Span span, FOType operand) {
//        if (this == DISCARD_FIRST) return operand;
//        switch (this) {
//            case MUL -> {
//                if (operand == FOType.I8) return FOType.I8;
//                if (operand == FOType.I32) return FOType.I32;
//                if (operand == FOType.F32) return FOType.F32;
//            }
//            case DIV -> {
//                if (operand == FOType.I8) return FOType.I8;
//                if (operand == FOType.I32) return FOType.I32;
//                if (operand == FOType.F32) return FOType.F32;
//            }
//            case MOD -> {
//                if (operand == FOType.I8) return FOType.I8;
//                if (operand == FOType.I32) return FOType.I32;
//            }
//            case ADD -> {
//                if (operand == FOType.I8) return FOType.I8;
//                if (operand == FOType.I32) return FOType.I32;
//                if (operand == FOType.F32) return FOType.F32;
//            }
//            case SUB -> {
//                if (operand == FOType.I8) return FOType.I8;
//                if (operand == FOType.I32) return FOType.I32;
//                if (operand == FOType.F32) return FOType.F32;
//            }
//            case LE, GE, LT, GT -> {
//                if (operand == FOType.I8) return FOType.BOOL;
//                if (operand == FOType.I32) return FOType.BOOL;
//                if (operand == FOType.F32) return FOType.BOOL;
//            }
//            case EQ, NEQ -> {
//                if (operand == FOType.I8) return FOType.BOOL;
//                if (operand == FOType.I32) return FOType.BOOL;
//                if (operand instanceof PointerType) return FOType.BOOL;
//                if (operand == FOType.F32) return FOType.BOOL;
//            }
//            case BITWISE_AND, BITWISE_XOR, BITWISE_OR -> {
//                if (operand == FOType.I8) return FOType.I8;
//                if (operand == FOType.I32) return FOType.I32;
//            }
//            case LOGICAL_AND, LOGICAL_OR -> {
//                if (operand == FOType.BOOL) return FOType.BOOL;
//            }
//        }
//        throw new AnalysisException(span, "cannot apply binary operator %s to %s".formatted(this, operand));
//    }
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