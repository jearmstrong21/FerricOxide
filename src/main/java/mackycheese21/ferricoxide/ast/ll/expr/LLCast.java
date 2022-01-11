package mackycheese21.ferricoxide.ast.ll.expr;

import mackycheese21.ferricoxide.ast.ll.type.LLType;

public class LLCast extends LLExpression {

    public final LLExpression value;
    public final LLType from;
    public final LLType target;

    public LLCast(LLType target, LLExpression value, LLType from) {
        this.value = value;
        this.target = target;
        this.from = from;
    }

    @Override
    public <T> T visit(LLExpressionVisitor<T> visitor) {
        return visitor.visitCast(this);
    }
}
