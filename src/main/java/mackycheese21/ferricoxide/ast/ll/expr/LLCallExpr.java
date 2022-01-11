package mackycheese21.ferricoxide.ast.ll.expr;

import java.util.List;

public class LLCallExpr extends LLExpression {

    public final LLExpression function;
    public final List<LLExpression> params;

    public LLCallExpr(LLExpression function, List<LLExpression> params) {
        this.function = function;
        this.params = params;
    }

    @Override
    public <T> T visit(LLExpressionVisitor<T> visitor) {
        return visitor.visitCallExpr(this);
    }
}
