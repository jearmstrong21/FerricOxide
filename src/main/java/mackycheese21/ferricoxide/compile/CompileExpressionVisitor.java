package mackycheese21.ferricoxide.compile;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.ast.IdentifierMap;
import mackycheese21.ferricoxide.ast.expr.*;
import mackycheese21.ferricoxide.ast.type.ConcreteType;
import mackycheese21.ferricoxide.ast.type.FunctionType;
import mackycheese21.ferricoxide.ast.type.PointerType;
import mackycheese21.ferricoxide.ast.type.StructType;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;
import mackycheese21.ferricoxide.ast.visitor.TypeValidatorVisitor;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.Map;

import static org.bytedeco.llvm.global.LLVM.*;

public class CompileExpressionVisitor implements ExpressionVisitor<LLVMValueRef> {

    private final LLVMBuilderRef builder;
    private final LLVMValueRef currentFunction;
    private final Map<String, LLVMValueRef> strings;
    private final IdentifierMap<ConcreteType> globalTypes;
    private final IdentifierMap<LLVMValueRef> globalRefs;
    private final IdentifierMap<StructType> structs;
    private final IdentifierMap<ConcreteType> variableTypes;
    private final IdentifierMap<LLVMValueRef> variableRefs;
    private final IdentifierMap<FunctionType> functionTypes;
    private final IdentifierMap<LLVMValueRef> functionRefs;
    private final TypeValidatorVisitor typeValidator;

    public CompileExpressionVisitor(LLVMBuilderRef builder, LLVMValueRef currentFunction, Map<String, LLVMValueRef> strings, IdentifierMap<ConcreteType> globalTypes, IdentifierMap<LLVMValueRef> globalRefs, IdentifierMap<StructType> structs, IdentifierMap<ConcreteType> variableTypes, IdentifierMap<LLVMValueRef> variableRefs,
                                    IdentifierMap<FunctionType> functionTypes, IdentifierMap<LLVMValueRef> functionRefs) {
        this.builder = builder;
        this.currentFunction = currentFunction;
        this.strings = strings;
        this.globalTypes = globalTypes;
        this.globalRefs = globalRefs;
        this.structs = structs;
        this.variableTypes = variableTypes;
        this.variableRefs = variableRefs;
        this.functionTypes = functionTypes;
        this.functionRefs = functionRefs;
        this.typeValidator = new TypeValidatorVisitor(globalTypes, structs, variableTypes, functionTypes);
    }

    private LLVMValueRef implicitResolve(ConcreteType expected, ConcreteType actual, LLVMValueRef valueRef) {
        while (actual instanceof PointerType pointer && expected != actual) {
            actual = pointer.to;
            valueRef = LLVMBuildLoad2(builder, actual.typeRef, valueRef, "deref");
        }
        if (expected == actual) return valueRef;
        return CastOperator.compile(builder, actual, expected, valueRef);
//        throw AnalysisException.incorrectType(expected, actual);
    }

    @Override
    public LLVMValueRef visitAccessVar(AccessVar accessVar) {
        LLVMValueRef valueRef;
        if (globalRefs.mapHas(accessVar.name)) valueRef = globalRefs.mapGet(accessVar.name);
        else valueRef = variableRefs.mapGet(accessVar.name);
        return LLVMBuildLoad2(builder, accessVar.visit(typeValidator).typeRef, valueRef, accessVar.name);
    }

    @Override
    public LLVMValueRef visitIntConstant(IntConstant intConstant) {
        return LLVMConstInt(ConcreteType.I32.typeRef, intConstant.value, 0);
    }

    @Override
    public LLVMValueRef visitUnaryExpr(UnaryExpr unaryExpr) {
        return unaryExpr.operator.compile(builder, unaryExpr.a.visit(this), unaryExpr.a.visit(typeValidator));
    }

    @Override
    public LLVMValueRef visitBinaryExpr(BinaryExpr binaryExpr) {
        ConcreteType typeA = binaryExpr.a.visit(typeValidator);
        ConcreteType typeB = binaryExpr.b.visit(typeValidator);
        return binaryExpr.operator.compile(builder,
                binaryExpr.a.visit(this),
                implicitResolve(typeA, typeB, binaryExpr.b.visit(this)),
                typeA);
    }

    @Override
    public LLVMValueRef visitIfExpr(IfExpr ifExpr) {
        LLVMBasicBlockRef then = LLVMAppendBasicBlock(currentFunction, "if_then");
        LLVMBasicBlockRef otherwise = LLVMAppendBasicBlock(currentFunction, "if_else");
        LLVMBasicBlockRef end = LLVMAppendBasicBlock(currentFunction, "if_end");

        LLVMTypeRef resultType = ifExpr.visit(typeValidator).typeRef;

        LLVMValueRef result = LLVMBuildAlloca(builder, resultType, "if_res");

        LLVMValueRef condition = ifExpr.condition.visit(this);

        LLVMBuildCondBr(builder, condition, then, otherwise);

        LLVMPositionBuilderAtEnd(builder, then);
        LLVMBuildStore(builder, ifExpr.then.visit(this), result);
        LLVMBuildBr(builder, end);

        LLVMPositionBuilderAtEnd(builder, otherwise);
        LLVMBuildStore(builder, ifExpr.otherwise.visit(this), result);
        LLVMBuildBr(builder, end);

        LLVMPositionBuilderAtEnd(builder, end);

        return LLVMBuildLoad2(builder, resultType, result, "if_res");
    }

    @Override
    public LLVMValueRef visitBoolConstant(BoolConstant boolConstant) {
        return LLVMConstInt(ConcreteType.BOOL.typeRef, boolConstant.value ? 1 : 0, 0);
    }

    @Override
    public LLVMValueRef visitCallExpr(CallExpr callExpr) {
        FunctionType functionType = functionTypes.mapGet(callExpr.name);
        ConcreteType result = callExpr.visit(typeValidator);
        PointerPointer<LLVMValueRef> args = new PointerPointer<>(callExpr.params.size());
        for (int i = 0; i < callExpr.params.size(); i++) {
            args.put(i, implicitResolve(
                    functionType.params.get(i),
                    callExpr.params.get(i).visit(typeValidator),
                    callExpr.params.get(i).visit(this)));
        }
        if (result.declarable) {
            return LLVMBuildCall2(builder, functionTypes.mapGet(callExpr.name).typeRef, functionRefs.mapGet(callExpr.name), args, callExpr.params.size(), callExpr.name);
        } else {
            LLVMBuildCall2(builder, functionTypes.mapGet(callExpr.name).typeRef, functionRefs.mapGet(callExpr.name), args, callExpr.params.size(), "");
            return null;
        }
    }

    public LLVMValueRef GEP(ConcreteType objectType, String fieldName, LLVMValueRef objectRef) {
        PointerType pointerType = AnalysisException.requirePointer(objectType);
        int index = pointerType.to.getFieldIndex(fieldName);
        if (index == -1) throw AnalysisException.noSuchField(objectType, fieldName);
        return LLVMBuildInBoundsGEP2(
                builder,
                pointerType.to.typeRef,
                objectRef,
                new PointerPointer<>(
                        new IntConstant(0).visit(this),
                        new IntConstant(index).visit(this)
                ),
                2,
                "gep_" + fieldName
        );
    }

    public LLVMValueRef GEP(ConcreteType objectType, LLVMValueRef index, LLVMValueRef objectRef) {
        PointerType pointerType = AnalysisException.requirePointer(objectType);
        return LLVMBuildInBoundsGEP2(
                builder,
                pointerType.to.typeRef,
                objectRef,
                new PointerPointer<>(
                        index
                ),
                1,
                "gep_" + LLVMGetValueName(index).getString()
        );
    }

    @Override
    public LLVMValueRef visitAccessField(AccessField accessField) {
        ConcreteType objectType = accessField.object.visit(typeValidator);
        String fieldName = accessField.field;
        LLVMValueRef objectRef = accessField.object.visit(this);

        PointerType pointerType = AnalysisException.requirePointer(objectType);
        int index = pointerType.to.getFieldIndex(fieldName);
        if (index == -1) throw AnalysisException.noSuchField(objectType, fieldName);
        LLVMValueRef GEP = LLVMBuildInBoundsGEP2(
                builder,
                pointerType.to.typeRef,
                objectRef,
                new PointerPointer<>(
                        new IntConstant(0).visit(this),
                        new IntConstant(index).visit(this)
                ),
                2,
                "gep_" + accessField.field
        );
        return LLVMBuildLoad2(builder, pointerType.to.getFieldType(fieldName).typeRef, GEP, accessField.field);
    }

    @Override
    public LLVMValueRef visitStructInit(StructInit structInit) {
        StructType struct = structs.mapGet(structInit.struct);
        PointerType pointer = PointerType.of(struct);
        LLVMValueRef structPtr = LLVMBuildAlloca(builder, struct.typeRef, struct.name);
        for (int i = 0; i < structInit.fieldNames.size(); i++) {
            LLVMValueRef fieldValue = structInit.fieldValues.get(i).visit(this);
            LLVMValueRef fieldPtr = GEP(pointer, structInit.fieldNames.get(i), structPtr);
            ConcreteType fieldType = structInit.fieldValues.get(i).visit(typeValidator);
            LLVMBuildStore(builder, implicitResolve(struct.getFieldType(structInit.fieldNames.get(i)), fieldType, fieldValue), fieldPtr);
        }
        return LLVMBuildLoad2(builder, struct.typeRef, structPtr, struct.name);
    }

    @Override
    public LLVMValueRef visitPointerDeref(PointerDeref pointerDeref) {
        return LLVMBuildLoad2(builder, pointerDeref.deref.visit(typeValidator).typeRef, pointerDeref.deref.visit(this), "deref");
    }

    @Override
    public LLVMValueRef visitCastExpr(CastExpr castExpr) {
        return CastOperator.compile(builder, castExpr.value.visit(typeValidator), castExpr.target, castExpr.value.visit(this));
    }

    @Override
    public LLVMValueRef visitAccessIndex(AccessIndex accessIndex) {
        PointerType arrayRefType = AnalysisException.requirePointer(accessIndex.value.visit(typeValidator)); // i32*
        ConcreteType arrayType = arrayRefType.to; // i32
//        LLVMValueRef load = LLVMBuildLoad2(builder, arrayRefType.typeRef, accessIndex.value.visit(this), "AccessIndex.load");
        return LLVMBuildLoad2(builder, arrayType.typeRef, LLVMBuildInBoundsGEP2(
                builder,
                arrayType.typeRef,
                accessIndex.value.visit(this),
                new PointerPointer<>(
                        new LLVMValueRef[]{accessIndex.index.visit(this)}
                ),
                1,
                "gep_[index]"
        ), "gep_[index]");
    }

    @Override
    public LLVMValueRef visitStringConstant(StringConstant stringConstant) {
        String str = StringConstant.unescape(stringConstant.value);
        LLVMValueRef valueRef;
        if (strings.containsKey(str)) valueRef = strings.get(str);
        else valueRef = LLVMBuildGlobalString(builder, str, "string");
        strings.put(str, valueRef);
//        return valueRef;
        // TODO: unhack this into ArrayType(ConcreteType, int) and cast from ArrayType <-> any pointer type <-> any pointer type
        return LLVMBuildInBoundsGEP2(builder,
//                LLVMPointerType(
                LLVMArrayType(ConcreteType.I8.typeRef, str.length()),
//                        0),
                valueRef,
                new PointerPointer<>(
                        new IntConstant(0).visit(this),
                        new IntConstant(0).visit(this)
                ),
                2, "string");
    }

    @Override
    public LLVMValueRef visitRefAccessVar(RefAccessVar refAccessVar) {
        if (globalRefs.mapHas(refAccessVar.name)) return globalRefs.mapGet(refAccessVar.name);
        return variableRefs.mapGet(refAccessVar.name);
    }

    @Override
    public LLVMValueRef visitRefAccessField(RefAccessField refAccessField) {
        ConcreteType objectType = refAccessField.object.visit(typeValidator);
        String fieldName = refAccessField.field;
        LLVMValueRef objectRef = refAccessField.object.visit(this);

        PointerType pointerType = AnalysisException.requirePointer(objectType);
        int index = pointerType.to.getFieldIndex(fieldName);
//        int index = objectType.to.getFieldIndex(fieldName);
        if (index == -1) throw AnalysisException.noSuchField(objectType, fieldName);
        return LLVMBuildInBoundsGEP2(
                builder,
                pointerType.to.typeRef,
//                objectType.typeRef,
                objectRef,
                new PointerPointer<>(
                        new IntConstant(0).visit(this),
                        new IntConstant(index).visit(this)
                ),
                2,
                "gep_" + refAccessField.field
        );
    }

    @Override
    public LLVMValueRef visitRefAccessIndex(RefAccessIndex refAccessIndex) {
        PointerType arrayRefType = AnalysisException.requirePointer(refAccessIndex.value.visit(typeValidator));
        ConcreteType arrayValueType = arrayRefType.to;
//        LLVMValueRef load = LLVMBuildLoad2(builder, arrayRefType.typeRef, refAccessIndex.value.visit(this), "AccessIndex.load");
        LLVMValueRef load = refAccessIndex.value.visit(this);
        return LLVMBuildInBoundsGEP2(
                builder,
                arrayValueType.typeRef,
                load,
                new PointerPointer<>(
                        new LLVMValueRef[]{refAccessIndex.index.visit(this)}
                ),
                1,
                "gep_[index]");
    }

    @Override
    public LLVMValueRef visitSizeOf(SizeOf sizeOf) {
        return LLVMBuildTrunc(builder, LLVMSizeOf(sizeOf.type.typeRef), ConcreteType.I32.typeRef, "sizeof");
    }

    @Override
    public LLVMValueRef visitZeroInit(ZeroInit zeroInit) {
        return LLVMConstNull(zeroInit.type.typeRef);
    }
}
