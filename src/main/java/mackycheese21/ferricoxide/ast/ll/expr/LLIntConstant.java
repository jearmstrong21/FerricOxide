package mackycheese21.ferricoxide.ast.ll.expr;

import mackycheese21.ferricoxide.ast.ll.type.LLType;

public class LLIntConstant extends LLExpression {

    public final long value;
    public final LLType target;

    public LLIntConstant(long value, LLType target) {
        this.value = value;
        this.target = target;
    }

    @Override
    public <T> T visit(LLExpressionVisitor<T> visitor) {
        return visitor.visitIntConstant(this);
    }
}
