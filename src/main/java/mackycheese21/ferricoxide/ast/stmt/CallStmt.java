package mackycheese21.ferricoxide.ast.stmt;

import mackycheese21.ferricoxide.ast.expr.CallExpr;
import mackycheese21.ferricoxide.ast.visitor.StatementVisitor;

public class CallStmt extends Statement {

    public final CallExpr callExpr;

    public CallStmt(CallExpr callExpr) {
        super(false);
        this.callExpr = callExpr;
    }

    @Override
    public <T> T visit(StatementVisitor<T> visitor) {
        return visitor.visitCallStmt(this);
    }
}
