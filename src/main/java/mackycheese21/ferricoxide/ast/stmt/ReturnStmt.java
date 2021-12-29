package mackycheese21.ferricoxide.ast.stmt;

import mackycheese21.ferricoxide.ast.expr.Expression;
import mackycheese21.ferricoxide.ast.visitor.StatementVisitor;
import mackycheese21.ferricoxide.parser.token.Span;

public class ReturnStmt extends Statement {

    public Expression value;

    public ReturnStmt(Span span, Expression value) {
        super(span, true);
        this.value = value;
    }

    @Override
    public <T> T visit(StatementVisitor<T> visitor) {
        return visitor.visitReturnStmt(this);
    }
}
