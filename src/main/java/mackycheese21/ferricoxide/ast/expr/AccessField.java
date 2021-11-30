package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;

public class AccessField extends Expression {

    public final Expression object;
    public final String field;

    public AccessField(Expression object, String field) {
        super(true);
        this.object = object;
        this.field = field;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitAccessField(this);
    }
}
