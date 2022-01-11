package mackycheese21.ferricoxide.ast.ll.stmt;

import mackycheese21.ferricoxide.ast.ll.expr.LLExpression;

public class LLReturn extends LLStatement {

    public final LLExpression value;

    public LLReturn(LLExpression value) {
        this.value = value;
    }

    @Override
    public void visit(LLStatementVisitor visitor) {
        visitor.visitReturn(this);
    }
}
