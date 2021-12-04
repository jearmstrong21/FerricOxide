package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.type.ConcreteType;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;

public class SizeOf extends Expression {

    public ConcreteType type;

    public SizeOf(ConcreteType type) {
        super(false);
        this.type = type;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitSizeOf(this);
    }
}
