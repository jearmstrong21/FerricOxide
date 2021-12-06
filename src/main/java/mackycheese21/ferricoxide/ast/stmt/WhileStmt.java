package mackycheese21.ferricoxide.ast.stmt;

import mackycheese21.ferricoxide.ast.expr.Expression;
import mackycheese21.ferricoxide.ast.visitor.StatementVisitor;

import java.util.List;

public class WhileStmt extends Statement {

    public final Expression condition;
    public final Block body;

    public WhileStmt(Expression condition, Block body) {
        super(false);
        this.condition = condition;
        this.body = body;
    }

    public static Block forStmt(Statement init, Expression condition, Statement update, Block body) {
        return new Block(List.of(
                init,
                new WhileStmt(condition, new Block(List.of(body, update)))
        ));
    }

    @Override
    public <T> T visit(StatementVisitor<T> visitor) {
        return visitor.visitWhileStmt(this);
    }
}
