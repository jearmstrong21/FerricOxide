package mackycheese21.ferricoxide.ast.ll.expr;

import mackycheese21.ferricoxide.ast.ll.type.LLStructType;
import mackycheese21.ferricoxide.ast.ll.type.LLType;

import java.util.List;

public class LLStructInit extends LLExpression {

    public final LLStructType target;
    public final List<LLExpression> values;

    public LLStructInit(LLStructType target, List<LLExpression> values) {
        this.target = target;
        this.values = values;
    }

    @Override
    public <T> T visit(LLExpressionVisitor<T> visitor) {
        return visitor.visitStructInit(this);
    }
}
