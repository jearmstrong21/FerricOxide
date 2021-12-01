package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;

public class RefAccessVar extends Expression {

    public final String name;

    public RefAccessVar(String name) {
        super(true);
        this.name = name;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitRefAccessVar(this);
    }
}
