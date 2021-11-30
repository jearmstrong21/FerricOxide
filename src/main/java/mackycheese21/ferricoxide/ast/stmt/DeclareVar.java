package mackycheese21.ferricoxide.ast.stmt;

import mackycheese21.ferricoxide.ast.type.ConcreteType;
import mackycheese21.ferricoxide.ast.expr.Expression;
import mackycheese21.ferricoxide.ast.visitor.StatementVisitor;

public class DeclareVar extends Statement {

    public ConcreteType type;
    public final String name;
    public final Expression value;

    public DeclareVar(ConcreteType type, String name, Expression value) {
        super(false);
        this.type = type;
        this.name = name;
        this.value = value;
    }

    @Override
    public <T> T visit(StatementVisitor<T> visitor) {
        return visitor.visitDeclareVar(this);
    }
}
