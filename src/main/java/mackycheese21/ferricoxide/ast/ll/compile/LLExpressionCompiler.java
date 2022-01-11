package mackycheese21.ferricoxide.ast.ll.compile;

import mackycheese21.ferricoxide.ast.ll.expr.*;
import mackycheese21.ferricoxide.ast.ll.type.LLPointerType;
import mackycheese21.ferricoxide.ast.ll.type.LLPrimitiveType;
import mackycheese21.ferricoxide.ast.ll.type.LLType;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;
import org.bytedeco.llvm.global.LLVM;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.bytedeco.llvm.global.LLVM.*;

public class LLExpressionCompiler implements LLExpressionVisitor<LLVMValueRef> {

    private final LLVMBuilderRef builder;
    private final LLVMValueRef currentFunction;
    private final LLTypeCompiler typeCompiler;
    private final Map<String, LLVMValueRef> globalRefs;
    private final List<LLVMValueRef> localRefs;
    private final Map<String, LLVMValueRef> functionRefs;

    public LLExpressionCompiler(LLVMBuilderRef builder, LLVMValueRef currentFunction, LLTypeCompiler typeCompiler, Map<String, LLVMValueRef> globalRefs, List<LLVMValueRef> localRefs, Map<String, LLVMValueRef> functionRefs) {
        this.builder = builder;
        this.currentFunction = currentFunction;
        this.typeCompiler = typeCompiler;
        this.globalRefs = globalRefs;
        this.localRefs = localRefs;
        this.functionRefs = functionRefs;
    }

    private LLPointerType requirePointer(LLType type) {
        if (type instanceof LLPointerType pointer) return pointer;
        throw new UnsupportedOperationException("" + type);
    }

    @Override
    public LLVMValueRef visitAccessLocal(LLAccess.Local expr) {
        return Objects.requireNonNull(localRefs.get(expr.index));
    }

    @Override
    public LLVMValueRef visitAccessGlobal(LLAccess.Global expr) {
        return Objects.requireNonNull(globalRefs.get(expr.name));
    }

    @Override
    public LLVMValueRef visitAccessFunction(LLAccess.Function expr) {
        return Objects.requireNonNull(functionRefs.get(expr.name));
    }

    @Override
    public LLVMValueRef visitAccessIndex(LLAccess.Index expr) {
        LLVMTypeRef elementType = requirePointer(expr.result).to.visit(typeCompiler);
        LLVMValueRef array = expr.array.visit(this);
        LLVMValueRef index = expr.index.visit(this);
        return LLVMBuildInBoundsGEP2(builder,
                elementType,
                array,
                new PointerPointer<>(1).put(new LLVMValueRef[]{index}),
                1, "gep");
    }

    @Override
    public LLVMValueRef visitAccessProperty(LLAccess.Property expr) {
        LLVMTypeRef objectType = requirePointer(expr.object.result).to.visit(typeCompiler);
        LLVMValueRef object = expr.object.visit(this);
        LLVMValueRef zero = LLVMConstInt(LLVMInt32Type(), 0, 0);
        LLVMValueRef index = LLVMConstInt(LLVMInt32Type(), expr.index, 0);
        return LLVMBuildInBoundsGEP2(builder,
                objectType,
                object,
                new PointerPointer<>(2).put(new LLVMValueRef[]{zero, index}),
                2, "gep");
    }

    private interface BinaryArithmeticOperator {
        LLVMValueRef apply(LLVMBuilderRef builder, LLVMValueRef left, LLVMValueRef right, String name);
    }

    private LLVMValueRef arithmeticOperator(LLBinary expr, BinaryArithmeticOperator f, BinaryArithmeticOperator i) {
        LLVMValueRef l = expr.left.visit(this);
        LLVMValueRef r = expr.right.visit(this);
        if (expr.result.isFloatingPoint()) return f.apply(builder, l, r, "binary");
        return i.apply(builder, l, r, "binary");
    }

    private LLVMValueRef booleanOperator(LLBinary expr, int floatCompare, int intCompare) {
        LLVMValueRef l = expr.left.visit(this);
        LLVMValueRef r = expr.right.visit(this);
        if (expr.result.isFloatingPoint()) return LLVMBuildFCmp(builder, floatCompare, l, r, "binary");
        return LLVMBuildICmp(builder, intCompare, l, r, "binary");
    }

    @Override
    public LLVMValueRef visitBinary(LLBinary expr) {
        return switch (expr.operator) {
            case MUL -> arithmeticOperator(expr, LLVM::LLVMBuildFMul, LLVM::LLVMBuildMul);
            case DIV -> arithmeticOperator(expr, LLVM::LLVMBuildFDiv, LLVM::LLVMBuildSDiv);
            case MOD -> arithmeticOperator(expr, LLVM::LLVMBuildFRem, LLVM::LLVMBuildSRem);
            case ADD -> arithmeticOperator(expr, LLVM::LLVMBuildFAdd, LLVM::LLVMBuildAdd);
            case SUB -> arithmeticOperator(expr, LLVM::LLVMBuildFSub, LLVM::LLVMBuildSub);
            case LE -> booleanOperator(expr, LLVMRealOLE, LLVMIntSLE);
            case LT -> booleanOperator(expr, LLVMRealOLT, LLVMIntSLT);
            case GE -> booleanOperator(expr, LLVMRealOGE, LLVMIntSGE);
            case GT -> booleanOperator(expr, LLVMRealOGT, LLVMIntSGT);
            case EQ -> booleanOperator(expr, LLVMRealOEQ, LLVMIntEQ);
            case NEQ -> booleanOperator(expr, LLVMRealONE, LLVMIntNE);
            case LOGICAL_AND, BITWISE_AND -> arithmeticOperator(expr, null, LLVM::LLVMBuildAnd);
            case BITWISE_XOR -> arithmeticOperator(expr, null, LLVM::LLVMBuildXor);
            case LOGICAL_OR, BITWISE_OR -> arithmeticOperator(expr, null, LLVM::LLVMBuildOr);
            case DISCARD_FIRST -> expr.right.visit(this);
        };
    }

    @Override
    public LLVMValueRef visitCallExpr(LLCallExpr expr) {
        String name = expr.result == LLPrimitiveType.VOID ? "" : "call";
        return LLVMBuildCall2(builder, expr.function.result.visit(typeCompiler), expr.function.visit(this),
                new PointerPointer<>(expr.params.size())
                        .put(expr.params.stream().map(e -> e.visit(this)).toArray(LLVMValueRef[]::new)),
                expr.params.size(), name);
    }

    @Override
    public LLVMValueRef visitCast(LLCast expr) {
        throw new UnsupportedOperationException(expr.toString());
    }

    @Override
    public LLVMValueRef visitDeref(LLDeref expr) {
        return LLVMBuildLoad2(builder, expr.result.visit(typeCompiler), expr.value.visit(this), "load");
    }

    @Override
    public LLVMValueRef visitFloatConstant(LLFloatConstant expr) {
        return LLVMConstReal(expr.target.visit(typeCompiler), expr.value);
    }

    @Override
    public LLVMValueRef visitIfExpr(LLIfExpr expr) {
        LLVMBasicBlockRef thenBlock = LLVMAppendBasicBlock(currentFunction, "then");
        LLVMBasicBlockRef otherwiseBlock = LLVMAppendBasicBlock(currentFunction, "otherwise");
        LLVMBasicBlockRef endBlock = LLVMAppendBasicBlock(currentFunction, "end");

        LLVMValueRef condition = expr.condition.visit(this);
        LLVMBuildCondBr(builder, condition, thenBlock, otherwiseBlock);

        LLVMPositionBuilderAtEnd(builder, thenBlock);
        LLVMValueRef then = expr.then.visit(this);
        LLVMBuildBr(builder, endBlock);

        LLVMPositionBuilderAtEnd(builder, otherwiseBlock);
        LLVMValueRef otherwise = expr.otherwise.visit(this);
        LLVMBuildBr(builder, endBlock);

        LLVMPositionBuilderAtEnd(builder, endBlock);
        LLVMValueRef result = LLVMBuildPhi(builder, expr.result.visit(typeCompiler), "phi");
        LLVMAddIncoming(result, new PointerPointer<>(2).put(new LLVMValueRef[]{
                then, otherwise
        }), new PointerPointer<>(2).put(new LLVMBasicBlockRef[]{
                thenBlock, otherwiseBlock
        }), 2);
        return result;
    }

    @Override
    public LLVMValueRef visitIntConstant(LLIntConstant expr) {
        return LLVMConstInt(expr.target.visit(typeCompiler), expr.value, 0);
    }

    @Override
    public LLVMValueRef visitSizeOf(LLSizeOf expr) {
        return LLVMSizeOf(expr.type.visit(typeCompiler));
    }

    @Override
    public LLVMValueRef visitStringConstant(LLStringConstant expr) {
        LLVMValueRef cnst = LLVMConstString(expr.value, expr.value.length(), 0);
        return LLVMBuildInBoundsGEP2(builder, LLVMArrayType(LLVMInt8Type(), expr.value.length()), cnst,
                new PointerPointer<>(2).put(new LLVMValueRef[]{
                        LLVMConstInt(LLVMInt32Type(), 0, 0),
                        LLVMConstInt(LLVMInt32Type(), 0, 0)
                }), 2, "gep");
    }

    @Override
    public LLVMValueRef visitStructInit(LLStructInit expr) {
        LLVMValueRef struct = LLVMBuildInsertValue(builder,
                LLVMGetUndef(expr.target.visit(typeCompiler)),
                expr.values.get(0).visit(this), 0, "insertvalue0");
        for (int i = 1; i < expr.values.size(); i++) {
            struct = LLVMBuildInsertValue(builder, struct,
                    expr.values.get(i).visit(this), i, "insertvalue" + i);
        }
        return struct;
    }

    private interface UnaryArithmeticOperator {
        LLVMValueRef apply(LLVMBuilderRef builder, LLVMValueRef operand, String name);
    }

    private LLVMValueRef arithmeticOperator(LLUnary expr, UnaryArithmeticOperator f, UnaryArithmeticOperator i) {
        LLVMValueRef operand = expr.operand.visit(this);
        if (expr.result.isFloatingPoint()) return f.apply(builder, operand, "unary");
        return i.apply(builder, operand, "unary");
    }

    @Override
    public LLVMValueRef visitUnary(LLUnary expr) {
        return switch (expr.operator) {
            case NEGATE -> arithmeticOperator(expr, LLVM::LLVMBuildFNeg, LLVM::LLVMBuildNeg);
            case LOGICAL_NOT, BITWISE_NOT -> LLVMBuildNot(builder, expr.operand.visit(this), "unary");
        };
    }

    @Override
    public LLVMValueRef visitZeroInit(LLZeroInit expr) {
        return LLVMConstNull(expr.target.visit(typeCompiler));
    }
}
