package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;

public class AccessVar extends Expression {

    public final String name;

    public AccessVar(String name) {
        super(true);
        this.name = name;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitAccessVar(this);
    }
}
