package mackycheese21.ferricoxide.ast.ll.expr;

import mackycheese21.ferricoxide.ast.ll.type.LLType;

public class LLSizeOf extends LLExpression {

    public final LLType type;

    public LLSizeOf(LLType type) {
        this.type = type;
    }

    @Override
    public <T> T visit(LLExpressionVisitor<T> visitor) {
        return visitor.visitSizeOf(this);
    }
}
