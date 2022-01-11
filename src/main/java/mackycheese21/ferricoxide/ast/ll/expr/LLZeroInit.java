package mackycheese21.ferricoxide.ast.ll.expr;

import mackycheese21.ferricoxide.ast.ll.type.LLType;

public class LLZeroInit extends LLExpression {

    public final LLType target;

    public LLZeroInit(LLType target) {
        this.target = target;
    }

    @Override
    public <T> T visit(LLExpressionVisitor<T> visitor) {
        return visitor.visitZeroInit(this);
    }
}
