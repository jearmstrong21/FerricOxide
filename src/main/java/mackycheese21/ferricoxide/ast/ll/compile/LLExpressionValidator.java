package mackycheese21.ferricoxide.ast.ll.compile;

import mackycheese21.ferricoxide.ast.ll.expr.*;
import mackycheese21.ferricoxide.ast.ll.type.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LLExpressionValidator implements LLExpressionVisitor<Void> {

    private final List<LLType> localTypes;
    private final Map<String, LLType> globalTypes;
    private final Map<String, LLFunctionType> functionTypes;

    public LLExpressionValidator(List<LLType> localTypes, Map<String, LLType> globalTypes, Map<String, LLFunctionType> functionTypes) {
        this.localTypes = localTypes;
        this.globalTypes = globalTypes;
        this.functionTypes = functionTypes;
    }

    private LLPointerType requirePointer(LLType type) {
        if (type instanceof LLPointerType pointer) return pointer;
        throw new UnsupportedOperationException("" + type);
    }

    private LLStructType requireStruct(LLType type) {
        if (type instanceof LLStructType struct) return struct;
        throw new UnsupportedOperationException();
    }

    private LLFunctionType requireFunction(LLType type) {
        if (type instanceof LLFunctionType function) return function;
        throw new UnsupportedOperationException("" + type);
    }

    private <T> void requireEqual(T actual, T expected) {
        if (!Objects.equals(actual, expected)) throw new UnsupportedOperationException();
    }

    private void validateList(List<LLType> types, List<LLExpression> expressions) {
        requireEqual(types.size(), expressions.size());
        for (int i = 0; i < types.size(); i++) {
            expressions.get(i).visit(this);
            requireEqual(expressions.get(i).result, types.get(i));
        }
    }

    @Override
    public Void visitAccessLocal(LLAccess.Local expr) {
        expr.result = new LLPointerType(localTypes.get(expr.index));
        return null;
    }

    @Override
    public Void visitAccessGlobal(LLAccess.Global expr) {
        expr.result = new LLPointerType(globalTypes.get(expr.name));
        return null;
    }

    @Override
    public Void visitAccessFunction(LLAccess.Function expr) {
        expr.result = functionTypes.get(expr.name);
        return null;
    }

    @Override
    public Void visitAccessIndex(LLAccess.Index expr) {
        expr.array.visit(this);
        expr.index.visit(this);
        requireEqual(expr.index.result, LLPrimitiveType.I32);
        expr.result = new LLPointerType(requirePointer(expr.array.result).to);
        return null;
    }

    @Override
    public Void visitAccessProperty(LLAccess.Property expr) {
        expr.object.visit(this);
        expr.result = new LLPointerType(requireStruct(requirePointer(expr.object.result).to).fields.get(expr.index));
        return null;
    }

    @Override
    public Void visitBinary(LLBinary expr) {
        expr.left.visit(this);
        expr.right.visit(this);
        requireEqual(expr.right.result, expr.left.result);
        expr.result = expr.left.result;
        return null;
    }

    @Override
    public Void visitCallExpr(LLCallExpr expr) {
        expr.function.visit(this);
        LLFunctionType function = requireFunction(expr.function.result);
        validateList(function.params, expr.params);
        expr.result = function.result;
        return null;
    }

    @Override
    public Void visitCast(LLCast expr) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Void visitDeref(LLDeref expr) {
        expr.value.visit(this);
        expr.result = requirePointer(expr.value.result).to;
        return null;
    }

    @Override
    public Void visitFloatConstant(LLFloatConstant expr) {
        expr.result = expr.target;
        return null;
    }

    @Override
    public Void visitIfExpr(LLIfExpr expr) {
        expr.condition.visit(this);
        expr.then.visit(this);
        expr.otherwise.visit(this);
        requireEqual(expr.condition.result, LLPrimitiveType.BOOL);
        requireEqual(expr.otherwise.result, expr.then.result);
        expr.result = expr.then.result;
        return null;
    }

    @Override
    public Void visitIntConstant(LLIntConstant expr) {
        expr.result = expr.target;
        return null;
    }

    @Override
    public Void visitSizeOf(LLSizeOf expr) {
        expr.result = LLPrimitiveType.I32;
        return null;
    }

    @Override
    public Void visitStringConstant(LLStringConstant expr) {
        expr.result = new LLPointerType(LLPrimitiveType.I8);
        return null;
    }

    @Override
    public Void visitStructInit(LLStructInit expr) {
        validateList(expr.target.fields, expr.values);
        expr.result = expr.target;
        return null;
    }

    @Override
    public Void visitUnary(LLUnary expr) {
        expr.operand.visit(this);
        expr.result = expr.operand.result;// FIXME: 1/9/22 validate llTypes with operators?
        return null;
    }

    @Override
    public Void visitZeroInit(LLZeroInit expr) {
        expr.result = expr.target;
        return null;
    }
}
