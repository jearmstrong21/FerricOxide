package mackycheese21.ferricoxide.ast.hl.expr;

public interface HLExpressionVisitor<T> {

    T visitAccessVar(HLAccess.Var expr);

    T visitAccessIndex(HLAccess.Index expr);

    T visitAccessPropertyName(HLAccess.Property.Name expr);

    T visitAccessPropertyIndex(HLAccess.Property.Index expr);

    T visitBinary(HLBinary expr);

    T visitBoolConstant(HLBoolConstant expr);

    T visitCallExpr(HLCallExpr expr);

    T visitCast(HLCast expr);

    T visitDeref(HLDeref expr);

    T visitFloatConstant(HLFloatConstant expr);

    T visitIfExpr(HLIfExpr expr);

    T visitIntConstant(HLIntConstant expr);

    T visitParen(HLParen expr);

    T visitSizeOf(HLSizeOf expr);

    T visitStringConstant(HLStringConstant expr);

    T visitStructInit(HLStructInit expr);

    T visitTupleInit(HLTupleInit expr);

    T visitUnary(HLUnary expr);

    T visitZeroInit(HLZeroInit expr);

}
