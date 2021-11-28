package mackycheese21.ferricoxide.ast.stmt;

import mackycheese21.ferricoxide.ast.expr.BinaryOperator;
import mackycheese21.ferricoxide.ast.expr.Expression;
import mackycheese21.ferricoxide.ast.visitor.StatementVisitor;

public class Assign extends Statement {

    public final Expression a;
    public final Expression b;
    public final BinaryOperator operator;

    public Assign(Expression a, Expression b, BinaryOperator operator) {
        super(false);
        this.a = a;
        this.b = b;
        this.operator = operator;
    }

    @Override
    public <T> T visit(StatementVisitor<T> visitor) {
        return visitor.visitAssign(this);
    }
}
