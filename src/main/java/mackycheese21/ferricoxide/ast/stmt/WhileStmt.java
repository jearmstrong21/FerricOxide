package mackycheese21.ferricoxide.ast.stmt;

import mackycheese21.ferricoxide.ast.expr.Expression;
import mackycheese21.ferricoxide.ast.visitor.StatementVisitor;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;

public class WhileStmt extends Statement {

    public Expression condition;
    public final Block body;

    public WhileStmt(Span span, Expression condition, Block body) {
        super(span, false);
        this.condition = condition;
        this.body = body;
    }

    @Override
    public <T> T visit(StatementVisitor<T> visitor) {
        return visitor.visitWhileStmt(this);
    }
}
