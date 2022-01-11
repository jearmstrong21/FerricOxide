package mackycheese21.ferricoxide.ast.ll.stmt;

import mackycheese21.ferricoxide.ast.ll.expr.LLCallExpr;

public class LLCallStmt extends LLStatement {

    public final LLCallExpr callExpr;

    public LLCallStmt(LLCallExpr callExpr) {
        this.callExpr = callExpr;
    }

    @Override
    public void visit(LLStatementVisitor visitor) {
        visitor.visitCallStmt(this);
    }
}
