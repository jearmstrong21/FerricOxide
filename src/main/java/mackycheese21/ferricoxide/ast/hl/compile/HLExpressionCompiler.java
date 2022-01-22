package mackycheese21.ferricoxide.ast.hl.compile;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.MapStack;
import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.hl.expr.*;
import mackycheese21.ferricoxide.ast.hl.mod.StructDef;
import mackycheese21.ferricoxide.ast.hl.type.*;
import mackycheese21.ferricoxide.ast.ll.expr.*;
import mackycheese21.ferricoxide.ast.ll.type.LLPrimitiveType;
import mackycheese21.ferricoxide.format.HLExpressionFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HLExpressionCompiler implements HLExpressionVisitor<LLExpression> {

    private final List<HLType> localTypes;
    private final MapStack<String, Integer> localIndices;

    private final Identifier modPath;
    private final HLTypeLookup typeLookup;

    public HLExpressionCompiler(
            List<HLType> localTypes,
            MapStack<String, Integer> localIndices,
            Identifier modPath,
            HLTypeLookup typeLookup) {
        this.localTypes = localTypes;
        this.localIndices = localIndices;
        this.modPath = modPath;
        this.typeLookup = typeLookup;
    }

    @Override
    public LLExpression visitAccessVar(HLAccess.Var expr) {
        // TODO refss
        if (expr.ref) throw new UnsupportedOperationException();
        if (expr.identifier.length() == 1) {
            if (localIndices.containsKey(expr.identifier.toString())) {
                expr.result = new HLPointerType(expr.span, localTypes.get(localIndices.get(expr.identifier.toString())));
                return new LLAccess.Local(localIndices.get(expr.identifier.toString()));
            }
        }
        Identifier localAttempt = Identifier.concat(modPath, expr.identifier);
        Identifier globalAttempt = expr.identifier;
        if (typeLookup.hlFunctions.containsKey(localAttempt)) {
            expr.result = typeLookup.hlFunctions.get(localAttempt).prototype.functionType();
            return new LLAccess.Function(localAttempt.toLLVMString());
        }
        if (typeLookup.hlFunctions.containsKey(globalAttempt)) {
            expr.result = typeLookup.hlFunctions.get(globalAttempt).prototype.functionType();
            return new LLAccess.Function(globalAttempt.toLLVMString());
        }
        if (typeLookup.hlGlobals.containsKey(localAttempt)) {
            expr.result = new HLPointerType(expr.span, typeLookup.hlGlobals.get(localAttempt).type);
            return new LLAccess.Global(localAttempt.toLLVMString());
        }
        if (typeLookup.hlGlobals.containsKey(globalAttempt)) {
            expr.result = new HLPointerType(expr.span, typeLookup.hlGlobals.get(globalAttempt).type);
            return new LLAccess.Global(globalAttempt.toLLVMString());
        }
        throw new AnalysisException(expr.span, "unknown identifier " + expr.identifier);
    }

    @Override
    public LLExpression visitAccessIndex(HLAccess.Index expr) {
        LLExpression array = expr.array.visit(this);
        LLExpression index = expr.index.visit(this);
        expr.result = expr.array.result.requirePointer(expr.array.span).to;
        expr.index.requireResult(new HLKeywordType(null, HLKeywordType.Type.I32));
        return new LLAccess.Index(array, index);
    }

    @Override
    public LLExpression visitAccessPropertyName(HLAccess.Property.Name expr) {
        LLExpression object = expr.object.visit(this);
        String fieldName = expr.name;

        Identifier struct = expr.object.result.requirePointer(expr.object.span).to.requireStruct(expr.object.span).identifier;
        StructDef structDef = typeLookup.lookupStructDef(struct);

        for (int i = 0; i < structDef.fields.size(); i++) {
            if (structDef.fields.get(i).x().equals(fieldName)) {
                expr.result = new HLPointerType(expr.span, structDef.fields.get(i).y());
                return new LLAccess.Property(object, i);
            }
        }
        throw new AnalysisException(expr.span, "no such field %s".formatted(fieldName));
    }

    @Override
    public LLExpression visitAccessPropertyIndex(HLAccess.Property.Index expr) {
        LLExpression object = expr.object.visit(this);
        if (expr.object.result instanceof HLTupleType tuple) {
            if (expr.index >= 0 && expr.index < tuple.types.size()) {
                expr.result = new HLPointerType(expr.span, tuple.types.get(expr.index));
                return new LLAccess.Property(object, expr.index);
            }
        }
        throw new AnalysisException(expr.span, "no index %s on type %s".formatted(expr.index, expr.object.result));
    }

    @Override
    public LLExpression visitBinary(HLBinary expr) {
        LLExpression left = expr.left.visit(this);
        LLExpression right = expr.right.visit(this);
        expr.right.requireResult(expr.left.result);
        // TODO validate operator
        expr.result = expr.left.result;
        return new LLBinary(left, expr.operator, right);
    }

    @Override
    public LLExpression visitBoolConstant(HLBoolConstant expr) {
        expr.result = new HLKeywordType(expr.span, HLKeywordType.Type.BOOL);
        return new LLIntConstant(expr.value ? 1 : 0, LLPrimitiveType.BOOL);
    }

    @Override
    public LLExpression visitCallExpr(HLCallExpr expr) {
        // TODO methods, traits, static methods
        LLExpression function = expr.function.visit(this);
        HLFunctionType functionType = expr.function.result.requireFunction(expr.function.span);
        if (functionType.params.size() != expr.params.size())
            throw new AnalysisException(expr.span, "expected %s arguments, got %s"
                    .formatted(functionType.params.size(), expr.params.size()));
        List<LLExpression> params = new ArrayList<>();
        for (int i = 0; i < functionType.params.size(); i++) {
            HLExpression param = expr.params.get(i);
            params.add(param.visit(this));
            param.requireResult(functionType.params.get(i));
        }
        expr.result = functionType.result;
        return new LLCallExpr(function, params);
    }

    @Override
    public LLExpression visitCast(HLCast expr) {
        // TODO validate cast
        LLExpression value = expr.value.visit(this);
        // TODO lookup the type
        expr.result = expr.target;
        return new LLCast(typeLookup.compile(modPath, expr.result), value);
    }

    @Override
    public LLExpression visitDeref(HLDeref expr) {
        LLExpression value = expr.value.visit(this);
        expr.result = expr.value.result.requirePointer(expr.span).to;
        return new LLDeref(value);
    }

    @Override
    public LLExpression visitFloatConstant(HLFloatConstant expr) {
        expr.result = new HLKeywordType(expr.span, HLKeywordType.Type.F32); // TODO other types for float constants as necessary
        return new LLFloatConstant(expr.value, LLPrimitiveType.F32);
    }

    @Override
    public LLExpression visitIfExpr(HLIfExpr expr) {
        LLExpression condition = expr.condition.visit(this);
        LLExpression then = expr.then.visit(this);
        LLExpression otherwise = expr.otherwise.visit(this);
        expr.condition.requireResult(new HLKeywordType(null, HLKeywordType.Type.I32));
        expr.otherwise.requireResult(expr.then.result);
        expr.result = expr.then.result;
        return new LLIfExpr(condition, then, otherwise);
    }

    @Override
    public LLExpression visitIntConstant(HLIntConstant expr) {
        expr.result = new HLKeywordType(expr.span, HLKeywordType.Type.I32);
        return new LLIntConstant(expr.value, LLPrimitiveType.I32);
    }

    @Override
    public LLExpression visitParen(HLParen expr) {
        LLExpression result = expr.expr.visit(this);
        expr.result = expr.expr.result;
        return result;
    }

    @Override
    public LLExpression visitSizeOf(HLSizeOf expr) {
        expr.result = new HLKeywordType(expr.span, HLKeywordType.Type.I32);
        return new LLSizeOf(typeLookup.compile(modPath, expr.target));
    }

    @Override
    public LLExpression visitStringConstant(HLStringConstant expr) {
        expr.result = new HLPointerType(expr.span, new HLKeywordType(expr.span, HLKeywordType.Type.I8));
        return new LLStringConstant(expr.value);
    }

    @Override
    public LLExpression visitStructInit(HLStructInit expr) {
        expr.struct = typeLookup.resolveStructDef(modPath, expr.struct);
        StructDef structDef = typeLookup.lookupStructDef(expr.struct);
        if (structDef.fields.size() != expr.fields.size())
            throw new AnalysisException(expr.span, "expected %s fields, got %s"
                    .formatted(structDef.fields.size(), expr.fields.size()));
        expr.result = new HLStructType(expr.struct);
        List<LLExpression> values = new ArrayList<>();
        for (int i = 0; i < structDef.fields.size(); i++) {
            String fieldName = structDef.fields.get(i).x();
            HLType fieldType = structDef.fields.get(i).y();
            // no need to resolve fieldType since all the structs already are done
            boolean found = false;
            for (int j = 0; j < expr.fields.size(); j++) {
                if (fieldName.equals(expr.fields.get(i).x())) {
                    HLExpression fieldValue = expr.fields.get(i).y();
                    LLExpression llValue = fieldValue.visit(this);
                    fieldValue.requireResult(fieldType);
                    values.add(llValue);
                    found = true;
                    break;
                }
            }
            if (!found) throw new AnalysisException(expr.span, "no field named %s found".formatted(fieldName));
        }
        return new LLStructInit(typeLookup.structCompiled.get(structDef.identifier), values);
    }

    @Override
    public LLExpression visitTupleInit(HLTupleInit expr) {
//        return null;
        throw new UnsupportedOperationException();
    }

    @Override
    public LLExpression visitUnary(HLUnary expr) {
        LLExpression operand = expr.operand.visit(this);
        expr.result = expr.operand.result;
        // TODO validate operator
        return new LLUnary(expr.operator, operand);
    }

    @Override
    public LLExpression visitZeroInit(HLZeroInit expr) {
        expr.result = expr.target;// TODO resolve type here and in sizeof
        return new LLZeroInit(typeLookup.compile(modPath, expr.target));
    }
}
