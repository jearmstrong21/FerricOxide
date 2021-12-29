package mackycheese21.ferricoxide.ast.visitor;

import mackycheese21.ferricoxide.ast.expr.*;
import mackycheese21.ferricoxide.ast.expr.unresolved.*;

public interface ExpressionVisitor<T> {

    T visitUnresolvedIntConstant(UnresolvedIntConstant unresolvedIntConstant);

    T visitUnresolvedFloatConstant(UnresolvedFloatConstant unresolvedFloatConstant);

    T visitUnresolvedAccessVar(UnresolvedAccessVar unresolvedAccessVar);

    T visitUnresolvedAccessProperty(UnresolvedAccessProperty unresolvedAccessProperty);

    T visitAccessVar(AccessVar accessVar);

    T visitAccessProperty(AccessProperty accessProperty);

    T visitIntConstant(IntConstant intConstant);

    T visitUnary(Unary unary);

    T visitBinary(Binary binary);

    T visitIfExpr(IfExpr ifExpr);

    T visitBoolConstant(BoolConstant boolConstant);

    T visitCallExpr(CallExpr callExpr);

    T visitUnresolvedStructInit(UnresolvedStructInit unresolvedStructInit);

    T visitPointerDeref(PointerDeref pointerDeref);

    T visitCastExpr(CastExpr castExpr);

    T visitStringConstant(StringConstant stringConstant);

    T visitSizeOf(SizeOf sizeOf);

    T visitZeroInit(ZeroInit zeroInit);

    T visitFloatConstant(FloatConstant floatConstant);

    T visitAggregateInit(AggregateInit aggregateInit);

    T visitArrayIndex(ArrayIndex arrayIndex);
}
