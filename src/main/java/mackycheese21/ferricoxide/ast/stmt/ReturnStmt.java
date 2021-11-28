package mackycheese21.ferricoxide.ast.stmt;

import mackycheese21.ferricoxide.ast.expr.Expression;
import mackycheese21.ferricoxide.ast.visitor.StatementVisitor;

public class ReturnStmt extends Statement {

    public final Expression value;

    public ReturnStmt(Expression value) {
        super(true);
        this.value = value;
    }

    @Override
    public <T> T visit(StatementVisitor<T> visitor) {
        return visitor.visitReturnStmt(this);
    }
}
