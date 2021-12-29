package mackycheese21.ferricoxide.ast.stmt;

import mackycheese21.ferricoxide.ast.expr.BinaryOperator;
import mackycheese21.ferricoxide.ast.expr.Expression;
import mackycheese21.ferricoxide.ast.visitor.StatementVisitor;
import mackycheese21.ferricoxide.parser.token.Span;

public class Assign extends Statement {

    public Expression a;
    public Expression b;
    public final BinaryOperator operator;

    public Assign(Span span, Expression a, Expression b, BinaryOperator operator) {
        super(span,false);
        this.a = a;
        this.b = b;
        this.operator = operator;
    }

    @Override
    public <T> T visit(StatementVisitor<T> visitor) {
        return visitor.visitAssign(this);
    }
}
