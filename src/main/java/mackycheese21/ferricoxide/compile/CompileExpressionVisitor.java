package mackycheese21.ferricoxide.compile;

import mackycheese21.ferricoxide.ast.ConcreteType;
import mackycheese21.ferricoxide.ast.IdentifierMap;
import mackycheese21.ferricoxide.ast.expr.*;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;
import mackycheese21.ferricoxide.ast.visitor.TypeValidatorVisitor;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import static org.bytedeco.llvm.global.LLVM.*;

public class CompileExpressionVisitor implements ExpressionVisitor<LLVMValueRef> {

    private final LLVMBuilderRef builder;
    private final LLVMValueRef currentFunction;
    private final IdentifierMap<ConcreteType> variableTypes;
    private final IdentifierMap<LLVMValueRef> variableRefs;
    private final IdentifierMap<ConcreteType.Function> functionTypes;
    private final IdentifierMap<LLVMValueRef> functionRefs;
    private final TypeValidatorVisitor typeValidator;

    public CompileExpressionVisitor(LLVMBuilderRef builder, LLVMValueRef currentFunction, IdentifierMap<ConcreteType> variableTypes, IdentifierMap<LLVMValueRef> variableRefs,
                                    IdentifierMap<ConcreteType.Function> functionTypes, IdentifierMap<LLVMValueRef> functionRefs) {
        this.builder = builder;
        this.currentFunction = currentFunction;
        this.variableTypes = variableTypes;
        this.variableRefs = variableRefs;
        this.functionTypes = functionTypes;
        this.functionRefs = functionRefs;
        this.typeValidator = new TypeValidatorVisitor(variableTypes, functionTypes);
    }

    @Override
    public LLVMValueRef visitAccessVar(AccessVar accessVar) {
        return LLVMBuildLoad2(builder, variableTypes.mapGet(accessVar.name).llvmTypeRef(), variableRefs.mapGet(accessVar.name), "AccessVar");
    }

    @Override
    public LLVMValueRef visitIntConstant(IntConstant intConstant) {
        return LLVMConstInt(ConcreteType.I32.llvmTypeRef(), intConstant.value, 0);
    }

    @Override
    public LLVMValueRef visitUnaryExpr(UnaryExpr unaryExpr) {
        return unaryExpr.operator.compile(builder, unaryExpr.a.visit(this), unaryExpr.visit(typeValidator));
    }

    @Override
    public LLVMValueRef visitBinaryExpr(BinaryExpr binaryExpr) {
        return binaryExpr.operator.compile(builder, binaryExpr.a.visit(this), binaryExpr.b.visit(this), binaryExpr.a.visit(typeValidator));
    }

    @Override
    public LLVMValueRef visitIfExpr(IfExpr ifExpr) {
        LLVMBasicBlockRef then = LLVMAppendBasicBlock(currentFunction, "IfExpr.then");
        LLVMBasicBlockRef otherwise = LLVMAppendBasicBlock(currentFunction, "IfExpr.otherwise");
        LLVMBasicBlockRef end = LLVMAppendBasicBlock(currentFunction, "IfExpr.end");

        LLVMTypeRef resultType = ifExpr.visit(typeValidator).llvmTypeRef();

        LLVMValueRef result = LLVMBuildAlloca(builder, resultType, "IfExpr.result");

        LLVMValueRef condition = ifExpr.condition.visit(this);

        LLVMBuildCondBr(builder, condition, then, otherwise);

        LLVMPositionBuilderAtEnd(builder, then);
        LLVMBuildStore(builder, ifExpr.then.visit(this), result);
        LLVMBuildBr(builder, end);

        LLVMPositionBuilderAtEnd(builder, otherwise);
        LLVMBuildStore(builder, ifExpr.otherwise.visit(this), result);
        LLVMBuildBr(builder, end);

        LLVMPositionBuilderAtEnd(builder, end);

        return LLVMBuildLoad2(builder, resultType, result, "IfExpr.result");
    }

    @Override
    public LLVMValueRef visitBoolConstant(BoolConstant boolConstant) {
        return LLVMConstInt(ConcreteType.BOOL.llvmTypeRef(), boolConstant.value ? 1 : 0, 0);
    }

    @Override
    public LLVMValueRef visitCallExpr(CallExpr callExpr) {
        callExpr.visit(typeValidator);
        PointerPointer<LLVMValueRef> args = new PointerPointer<>(callExpr.params.size());
        for (int i = 0; i < callExpr.params.size(); i++) {
            args.put(i, callExpr.params.get(i).visit(this));
        }
        return LLVMBuildCall2(builder, functionTypes.mapGet(callExpr.name).llvmTypeRef(), functionRefs.mapGet(callExpr.name), args, callExpr.params.size(), "CallExpr");
    }
}
