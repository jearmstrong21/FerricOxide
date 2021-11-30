package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;

public abstract class Expression {

    @Deprecated
    public final boolean lvalue;

    protected Expression(boolean lvalue) {
        this.lvalue = lvalue;
    }

    public abstract <T> T visit(ExpressionVisitor<T> visitor);

}
