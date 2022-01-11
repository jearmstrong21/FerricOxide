package mackycheese21.ferricoxide.ast.ll.expr;

public class LLDeref extends LLExpression {

    public final LLExpression value;

    public LLDeref(LLExpression value) {
        this.value = value;
    }

    @Override
    public <T> T visit(LLExpressionVisitor<T> visitor) {
        return visitor.visitDeref(this);
    }
}
