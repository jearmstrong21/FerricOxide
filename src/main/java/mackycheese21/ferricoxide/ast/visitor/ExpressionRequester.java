package mackycheese21.ferricoxide.ast.visitor;

import mackycheese21.ferricoxide.ast.expr.*;
import mackycheese21.ferricoxide.ast.expr.unresolved.*;

public interface ExpressionRequester<T, U> {

    T visitUnresolvedIntConstant(U request, UnresolvedIntConstant unresolvedIntConstant);

    T visitUnresolvedFloatConstant(U request, UnresolvedFloatConstant unresolvedFloatConstant);

    T visitUnresolvedAccessVar(U request, UnresolvedAccessVar unresolvedAccessVar);

    T visitUnresolvedAccessProperty(U request, UnresolvedAccessProperty unresolvedAccessProperty);

    T visitAccessVar(U request, AccessVar accessVar);

    T visitAccessProperty(U request, AccessProperty accessProperty);

    T visitIntConstant(U request, IntConstant intConstant);

    T visitUnary(U request, Unary unary);

    T visitBinary(U request, Binary binary);

    T visitIfExpr(U request, IfExpr ifExpr);

    T visitBoolConstant(U request, BoolConstant boolConstant);

    T visitCallExpr(U request, CallExpr callExpr);

    T visitUnresolvedStructInit(U request, UnresolvedStructInit unresolvedStructInit);

    T visitPointerDeref(U request, PointerDeref pointerDeref);

    T visitCastExpr(U request, CastExpr castExpr);

    T visitStringConstant(U request, StringConstant stringConstant);

    T visitSizeOf(U request, SizeOf sizeOf);

    T visitZeroInit(U request, ZeroInit zeroInit);

    T visitFloatConstant(U request, FloatConstant floatConstant);

    T visitAggregateInit(U request, AggregateInit aggregateInit);

    T visitArrayIndex(U request, ArrayIndex arrayIndex);
}
