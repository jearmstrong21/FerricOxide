package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;

public class RefAccessVar extends Expression {

    public final Identifier[] names;

    public RefAccessVar(Identifier[] names) {
        super(true);
        this.names = names;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitRefAccessVar(this);
    }
}
