package mackycheese21.ferricoxide.ast.ll.expr;

public interface LLExpressionVisitor<T> {
    T visitAccessLocal(LLAccess.Local expr);

    T visitAccessGlobal(LLAccess.Global expr);

    T visitAccessFunction(LLAccess.Function expr);

    T visitAccessIndex(LLAccess.Index expr);

    T visitAccessProperty(LLAccess.Property expr);

    T visitBinary(LLBinary expr);

    T visitCallExpr(LLCallExpr expr);

    T visitCast(LLCast expr);

    T visitDeref(LLDeref expr);

    T visitFloatConstant(LLFloatConstant expr);

    T visitIfExpr(LLIfExpr expr);

    T visitIntConstant(LLIntConstant expr);

    T visitSizeOf(LLSizeOf expr);

    T visitStringConstant(LLStringConstant expr);

    T visitStructInit(LLStructInit expr);

    T visitUnary(LLUnary expr);

    T visitZeroInit(LLZeroInit expr);
}