package mackycheese21.ferricoxide.ast.stmt;

import mackycheese21.ferricoxide.ast.expr.CallExpr;
import mackycheese21.ferricoxide.ast.visitor.StatementVisitor;
import mackycheese21.ferricoxide.parser.token.Span;

public class CallStmt extends Statement {

    public CallExpr callExpr;

    public CallStmt(Span span, CallExpr callExpr) {
        super(span, false);
        this.callExpr = callExpr;
    }

    @Override
    public void visit(StatementVisitor visitor) {
        visitor.visitCallStmt(this);
    }
}
