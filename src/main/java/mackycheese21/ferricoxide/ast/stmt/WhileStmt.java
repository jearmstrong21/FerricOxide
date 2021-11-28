package mackycheese21.ferricoxide.ast.stmt;

import mackycheese21.ferricoxide.ast.expr.Expression;
import mackycheese21.ferricoxide.ast.visitor.StatementVisitor;

public class WhileStmt extends Statement {

    public final Expression condition;
    public final Block body;

    public WhileStmt(Expression condition, Block body) {
        super(false);
        this.condition = condition;
        this.body = body;
    }


    @Override
    public <T> T visit(StatementVisitor<T> visitor) {
        return visitor.visitWhileStmt(this);
    }
}
