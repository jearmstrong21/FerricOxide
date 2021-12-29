package mackycheese21.ferricoxide.ast.stmt;

import mackycheese21.ferricoxide.ast.expr.Expression;
import mackycheese21.ferricoxide.ast.visitor.StatementVisitor;
import mackycheese21.ferricoxide.parser.token.Span;

public class IfStmt extends Statement {

    public Expression condition;
    public final Block then;
    public final Block otherwise;

    public IfStmt(Span span, Expression condition, Block then, Block otherwise) {
        super(span, then.terminal && otherwise != null && otherwise.terminal);
        this.condition = condition;
        this.then = then;
        this.otherwise = otherwise;
    }

    @Override
    public <T> T visit(StatementVisitor<T> visitor) {
        return visitor.visitIfStmt(this);
    }
}
