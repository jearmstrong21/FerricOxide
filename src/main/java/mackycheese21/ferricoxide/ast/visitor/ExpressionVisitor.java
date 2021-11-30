package mackycheese21.ferricoxide.ast.visitor;

import mackycheese21.ferricoxide.ast.expr.*;

public interface ExpressionVisitor<T> {

    T visitAccessVar(AccessVar accessVar);

    T visitIntConstant(IntConstant intConstant);

    T visitUnaryExpr(UnaryExpr unaryExpr);

    T visitBinaryExpr(BinaryExpr binaryExpr);

    T visitIfExpr(IfExpr ifExpr);

    T visitBoolConstant(BoolConstant boolConstant);

    T visitCallExpr(CallExpr callExpr);

    T visitAccessField(AccessField accessField);

    T visitStructInit(StructInit structInit);

    T visitPointerDeref(PointerDeref pointerDeref);

    T visitCastExpr(CastExpr castExpr);

    T visitIndexExpr(IndexExpr indexExpr);

    T visitStringConstant(StringConstant stringConstant);

}
