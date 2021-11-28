package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;

import java.util.List;

public class CallExpr extends Expression {

    public final String name;
    public final List<Expression> params;

    public CallExpr(String name, List<Expression> params) {
        super(false);
        this.name = name;
        this.params = params;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitCallExpr(this);
    }
}
