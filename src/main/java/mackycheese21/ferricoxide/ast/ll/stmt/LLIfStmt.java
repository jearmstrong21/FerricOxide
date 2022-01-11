package mackycheese21.ferricoxide.ast.ll.stmt;

import mackycheese21.ferricoxide.ast.ll.expr.LLExpression;

import java.util.List;

public class LLIfStmt extends LLStatement {

    public final LLExpression condition;
    public final List<LLStatement> then;
    public final List<LLStatement> otherwise;

    public LLIfStmt(LLExpression condition, List<LLStatement> then, List<LLStatement> otherwise) {
        this.condition = condition;
        this.then = then;
        this.otherwise = otherwise;
    }

    @Override
    public void visit(LLStatementVisitor visitor) {
        visitor.visitIfStmt(this);
    }
}
