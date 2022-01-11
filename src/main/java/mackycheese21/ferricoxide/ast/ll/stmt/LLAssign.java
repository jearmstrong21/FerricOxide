package mackycheese21.ferricoxide.ast.ll.stmt;

import mackycheese21.ferricoxide.ast.ll.expr.LLExpression;

public class LLAssign extends LLStatement {

    public final LLExpression left;
    public final LLExpression right;

    public LLAssign(LLExpression left, LLExpression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public void visit(LLStatementVisitor visitor) {
        visitor.visitAssign(this);
    }
}
