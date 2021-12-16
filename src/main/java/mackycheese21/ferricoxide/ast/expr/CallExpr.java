package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;

import java.util.List;

public class CallExpr extends Expression {

    public final Expression function;
    public final List<Expression> params;

    public CallExpr(Expression function, List<Expression> params) {
        super(false);
        this.function = function;
        this.params = params;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitCallExpr(this);
    }
}
