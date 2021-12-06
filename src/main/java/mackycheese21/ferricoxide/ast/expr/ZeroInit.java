package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.type.ConcreteType;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;

public class ZeroInit extends Expression {

    public ConcreteType type;

    public ZeroInit(ConcreteType type) {
        super(false);
        this.type = type;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitZeroInit(this);
    }
}
