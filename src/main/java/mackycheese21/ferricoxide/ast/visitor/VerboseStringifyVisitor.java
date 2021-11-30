package mackycheese21.ferricoxide.ast.visitor;

import mackycheese21.ferricoxide.ast.expr.*;

import java.util.stream.Collectors;

public class VerboseStringifyVisitor implements ExpressionVisitor<String> {
    @Override
    public String visitAccessVar(AccessVar accessVar) {
        return "access[%s]".formatted(accessVar.name);
    }

    @Override
    public String visitIntConstant(IntConstant intConstant) {
        return "i32[%s]".formatted(intConstant.value);
    }

    @Override
    public String visitUnaryExpr(UnaryExpr unaryExpr) {
        return "%s[%s]".formatted(unaryExpr.operator, unaryExpr.a.visit(this));
    }

    @Override
    public String visitBinaryExpr(BinaryExpr binaryExpr) {
        return "%s[%s, %s]".formatted(binaryExpr.operator, binaryExpr.a.visit(this), binaryExpr.b.visit(this));
    }

    @Override
    public String visitIfExpr(IfExpr ifExpr) {
        return "if(%s) { %s } else { %s }".formatted(ifExpr.condition.visit(this), ifExpr.then.visit(this), ifExpr.otherwise.visit(this));
    }

    @Override
    public String visitBoolConstant(BoolConstant boolConstant) {
        return "%s".formatted(boolConstant.value);
    }

    @Override
    public String visitCallExpr(CallExpr callExpr) {
        return "%s(%s)".formatted(callExpr.name, callExpr.params.stream().map(expr -> expr.visit(this)).collect(Collectors.joining(", ")));
    }

    @Override
    public String visitAccessField(AccessField accessField) {
        return "%s.%s".formatted(accessField.object.visit(this), accessField.field);
    }

    @Override
    public String visitStructInit(StructInit structInit) {
        return new StringifyVisitor("").visitStructInit(structInit);
    }

    @Override
    public String visitPointerDeref(PointerDeref pointerDeref) {
        return "deref[%s]".formatted(pointerDeref.visit(this));
    }

    @Override
    public String visitCastExpr(CastExpr castExpr) {
        return "cast[%s -> %s]".formatted(castExpr.target, castExpr.value.visit(this));
    }

    @Override
    public String visitIndexExpr(IndexExpr indexExpr) {
        return "index[%s: [%s]]".formatted(indexExpr.value, indexExpr.index);
    }
}
