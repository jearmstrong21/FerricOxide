package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;

public class AccessVar extends Expression {

    public final Identifier[] names;

    public AccessVar(Identifier[] names) {
        super(false);
        this.names = names;
    }

    @Override
    public Expression makeLValue() {
        return new RefAccessVar(names);
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitAccessVar(this);
    }
}
