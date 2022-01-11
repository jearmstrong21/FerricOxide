package mackycheese21.ferricoxide.ast.ll.expr;

public class LLStringConstant extends LLExpression {

    public final String value;

    public LLStringConstant(String value) {
        this.value = value;
    }

    @Override
    public <T> T visit(LLExpressionVisitor<T> visitor) {
        return visitor.visitStringConstant(this);
    }
}
