package mackycheese21.ferricoxide.ast.hl.stmt;

import mackycheese21.ferricoxide.ast.hl.expr.HLCallExpr;
import mackycheese21.ferricoxide.parser.token.Span;

public class HLCallStmt extends HLStatement {

    public final HLCallExpr callExpr;

    public HLCallStmt(Span span, HLCallExpr callExpr) {
        super(span);
        this.callExpr = callExpr;
    }

    @Override
    public <T> T visit(HLStatementVisitor<T> visitor) {
        return visitor.visitCallStmt(this);
    }
}
