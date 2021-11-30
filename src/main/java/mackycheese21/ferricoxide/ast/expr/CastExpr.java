package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.type.ConcreteType;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;

public class CastExpr extends Expression {

    public final ConcreteType target;
    public final Expression value;

    public CastExpr(ConcreteType target, Expression value) {
        super(false);
        this.target = target;
        this.value = value;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitCastExpr(this);
    }
}
