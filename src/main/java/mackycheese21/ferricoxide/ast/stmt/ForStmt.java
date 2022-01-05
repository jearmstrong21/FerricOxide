package mackycheese21.ferricoxide.ast.stmt;

import mackycheese21.ferricoxide.ast.expr.Expression;
import mackycheese21.ferricoxide.ast.visitor.StatementVisitor;
import mackycheese21.ferricoxide.parser.token.Span;

public class ForStmt extends Statement {

    public final Statement init;
    public Expression condition;
    public final Statement update;
    public final Block body;

    public ForStmt(Span span, Statement init, Expression condition, Statement update, Block body) {
        super(span, false);
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }

    @Override
    public <T> void visit(StatementVisitor<T> visitor) {
        visitor.visitForStmt(this);
    }
}
