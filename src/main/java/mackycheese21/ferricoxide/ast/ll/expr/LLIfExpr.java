package mackycheese21.ferricoxide.ast.ll.expr;

public class LLIfExpr extends LLExpression {

    public final LLExpression condition;
    public final LLExpression then;
    public final LLExpression otherwise;

    public LLIfExpr(LLExpression condition, LLExpression then, LLExpression otherwise) {
        this.condition = condition;
        this.then = then;
        this.otherwise = otherwise;
    }

    @Override
    public <T> T visit(LLExpressionVisitor<T> visitor) {
        return visitor.visitIfExpr(this);
    }
}
