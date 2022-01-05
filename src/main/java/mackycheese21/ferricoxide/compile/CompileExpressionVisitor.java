package mackycheese21.ferricoxide.compile;

import mackycheese21.ferricoxide.MapStack;
import mackycheese21.ferricoxide.Utils;
import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.expr.*;
import mackycheese21.ferricoxide.ast.expr.unresolved.*;
import mackycheese21.ferricoxide.ast.type.FOType;
import mackycheese21.ferricoxide.ast.type.TypeRegistry;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import static org.bytedeco.llvm.global.LLVM.*;

public record CompileExpressionVisitor(LLVMBuilderRef builder,
                                       LLVMValueRef currentFunction,
                                       Map<Identifier, FOType> globalTypes,
                                       Map<Identifier, LLVMValueRef> globalRefs,
                                       Map<Identifier, LLVMValueRef> functionRefs,
                                       MapStack<Identifier, FOType> localTypes,
                                       MapStack<Identifier, LLVMValueRef> localRefs) implements ExpressionVisitor<LLVMValueRef> {

    @Override
    public LLVMValueRef visitUnresolvedIntConstant(UnresolvedIntConstant unresolvedIntConstant) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LLVMValueRef visitUnresolvedFloatConstant(UnresolvedFloatConstant unresolvedFloatConstant) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LLVMValueRef visitUnresolvedAccessVar(UnresolvedAccessVar unresolvedAccessVar) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LLVMValueRef visitUnresolvedAccessProperty(UnresolvedAccessProperty unresolvedAccessProperty) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LLVMValueRef visitAccessVar(AccessVar accessVar) {
        switch (accessVar.type) {
            case GLOBAL -> {
                LLVMValueRef global = globalRefs.get(accessVar.name);
                if (!accessVar.reference) {
                    global = LLVMBuildLoad2(builder, TypeRegistry.forceLookup(globalTypes.get(accessVar.name)), global, "global");
                }
                return global;
            }
            case FUNCTION -> {
                LLVMValueRef function = functionRefs.get(accessVar.name);
                Utils.assertFalse(accessVar.reference);
                return function;
            }
            case LOCAL -> {
                LLVMValueRef local = localRefs.get(accessVar.name);
                if (!accessVar.reference) {
                    local = LLVMBuildLoad2(builder, TypeRegistry.forceLookup(localTypes.get(accessVar.name)), local, "local");
                }
                return local;
            }
            default -> throw new UnsupportedOperationException();
        }
    }

    @Override
    public LLVMValueRef visitAccessProperty(AccessProperty accessProperty) {
        LLVMValueRef aggregate = accessProperty.aggregate.visit(this);
        FOType aggType = Utils.expectPointer(null, accessProperty.aggregate.result).to;
//        if (accessProperty.derefAggregate) {
//            throw new UnsupportedOperationException();
//        }
//            aggType = ((PointerType) aggType).to;
//            aggregate = LLVMBuildLoad2(builder, TypeRegistry.forceLookup(aggType), aggregate, "load");
//        }
        int index = accessProperty.index;
        LLVMTypeRef property = TypeRegistry.forceLookup(new ArrayList<>(aggType.fields.values()).get(index));
        LLVMTypeRef aggTypeRef = TypeRegistry.forceLookup(aggType);
        LLVMValueRef result = LLVMBuildInBoundsGEP2(builder, aggTypeRef, aggregate, new PointerPointer<>(
                LLVMConstInt(LLVMInt32Type(), 0, 0),
                LLVMConstInt(LLVMInt32Type(), index, 0)
        ), 2, "gep");
        if (!accessProperty.ref) {
            result = LLVMBuildLoad2(builder, property, result, "load");
        }
        return result;
    }

    @Override
    public LLVMValueRef visitIntConstant(IntConstant intConstant) {
        return LLVMConstInt(TypeRegistry.forceLookup(intConstant.type), intConstant.value, 0);
    }

    @Override
    public LLVMValueRef visitUnary(Unary unary) {
        return unary.operator.compile(builder, unary.a.visit(this), unary.a.result);
    }

    @Override
    public LLVMValueRef visitBinary(Binary binary) {
        return binary.operator.compile(builder,
                binary.a.visit(this),
                binary.b.visit(this),
                binary.a.result
        );
    }

    @Override
    public LLVMValueRef visitIfExpr(IfExpr ifExpr) {
        LLVMBasicBlockRef then = LLVMAppendBasicBlock(currentFunction, "then");
        LLVMBasicBlockRef otherwise = LLVMAppendBasicBlock(currentFunction, "else");
        LLVMBasicBlockRef end = LLVMAppendBasicBlock(currentFunction, "end");

        LLVMValueRef alloca = LLVMBuildAlloca(builder, TypeRegistry.forceLookup(ifExpr.result), "result");

        LLVMValueRef condition = ifExpr.condition.visit(this);
        LLVMBuildCondBr(builder, condition, then, otherwise);

        LLVMPositionBuilderAtEnd(builder, then);
        LLVMBuildStore(builder, ifExpr.then.visit(this), alloca);
        LLVMBuildBr(builder, end);

        LLVMPositionBuilderAtEnd(builder, otherwise);
        LLVMBuildStore(builder, ifExpr.otherwise.visit(this), alloca);
        LLVMBuildBr(builder, end);

        LLVMPositionBuilderAtEnd(builder, end);
        return LLVMBuildLoad2(builder, TypeRegistry.forceLookup(ifExpr.result), alloca, "load");
    }

    @Override
    public LLVMValueRef visitBoolConstant(BoolConstant boolConstant) {
        return LLVMConstInt(LLVMInt1Type(), boolConstant.value ? 1 : 0, 0);
    }

    @Override
    public LLVMValueRef visitCallExpr(CallExpr callExpr) {
        LLVMValueRef function = callExpr.function.visit(this);
        LLVMValueRef[] args = callExpr.params.stream().map(e -> e.visit(this)).collect(Collectors.toList()).toArray(LLVMValueRef[]::new);
        String name;
        if (callExpr.result.equals(FOType.VOID)) {
            name = "";
        } else {
            name = "call";
        }
        return LLVMBuildCall2(builder, TypeRegistry.forceLookup(callExpr.function.result), function, new PointerPointer<>(args.length).put(args), args.length, name);
    }

    @Override
    public LLVMValueRef visitUnresolvedStructInit(UnresolvedStructInit unresolvedStructInit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LLVMValueRef visitPointerDeref(PointerDeref pointerDeref) {
        return LLVMBuildLoad2(builder, TypeRegistry.forceLookup(pointerDeref.result), pointerDeref.deref.visit(this), "deref");
    }

    @Override
    public LLVMValueRef visitCastExpr(CastExpr castExpr) {
        return CastOperator.compile(builder, castExpr.value.result, castExpr.target, castExpr.value.visit(this));
    }

    @Override
    public LLVMValueRef visitStringConstant(StringConstant stringConstant) {
        String s = stringConstant.value;
//        System.out.println("unescaped str " + s);
        LLVMValueRef str = LLVMBuildGlobalString(builder, s, "str");
        return LLVMBuildInBoundsGEP2(builder, LLVMArrayType(LLVMInt8Type(), s.length()), str,
                new PointerPointer<>(
                        LLVMConstInt(LLVMInt32Type(), 0, 0),
                        LLVMConstInt(LLVMInt32Type(), 0, 0)
                )
                , 2, "gep");
//        return LLVMBuildBitCast(builder, LLVMConstString(stringConstant.value, stringConstant.value.length(), 0), TypeRegistry.forceLookup(new PointerType(FOType.I8)), "bitcast");
    }

    @Override
    public LLVMValueRef visitSizeOf(SizeOf sizeOf) {
        return LLVMBuildTrunc(builder, LLVMSizeOf(TypeRegistry.forceLookup(sizeOf.type)), LLVMInt32Type(), "trunc");
    }

    @Override
    public LLVMValueRef visitZeroInit(ZeroInit zeroInit) {
        return LLVMConstNull(TypeRegistry.forceLookup(zeroInit.type));
    }

    @Override
    public LLVMValueRef visitFloatConstant(FloatConstant floatConstant) {
        return LLVMConstReal(TypeRegistry.forceLookup(floatConstant.type), floatConstant.value);
    }

    @Override
    public LLVMValueRef visitAggregateInit(AggregateInit aggregateInit) {
        LLVMTypeRef llvmType = TypeRegistry.forceLookup(aggregateInit.type);
        LLVMValueRef tuple = LLVMBuildAlloca(builder, llvmType, "alloca");
        for (int i = 0; i < aggregateInit.values.size(); i++) {
            LLVMBuildStore(builder, aggregateInit.values.get(i).visit(this),
                    LLVMBuildInBoundsGEP2(builder, llvmType, tuple, new PointerPointer<>(2).put(
                            LLVMConstInt(LLVMInt32Type(), 0, 0),
                            LLVMConstInt(LLVMInt32Type(), i, 0)
                    ), 2, "gep")
            );
        }
        return LLVMBuildLoad2(builder, llvmType, tuple, "load");
    }

    @Override
    public LLVMValueRef visitArrayIndex(ArrayIndex arrayIndex) {
        LLVMValueRef array = arrayIndex.array.visit(this);
        LLVMValueRef index = arrayIndex.index.visit(this);
        LLVMValueRef result = LLVMBuildInBoundsGEP2(builder, TypeRegistry.forceLookup(Utils.expectPointer(null, arrayIndex.array.result).to), array, new PointerPointer<>(1).put(index), 1, "gep");
        if (!arrayIndex.ref)
            result = LLVMBuildLoad2(builder, TypeRegistry.forceLookup(Utils.expectPointer(null, arrayIndex.array.result).to), result, "load");
        return result;
    }
}
