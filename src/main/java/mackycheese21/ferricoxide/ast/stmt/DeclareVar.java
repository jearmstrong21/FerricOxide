package mackycheese21.ferricoxide.ast.stmt;

import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.expr.Expression;
import mackycheese21.ferricoxide.ast.type.FOType;
import mackycheese21.ferricoxide.ast.visitor.StatementVisitor;
import mackycheese21.ferricoxide.parser.token.Span;

public class DeclareVar extends Statement {

    public FOType type;
    public final Identifier name;
    public Expression value;

    public DeclareVar(Span span, FOType type, Identifier name, Expression value) {
        super(span, false);
        this.type = type;
        this.name = name;
        this.value = value;
    }

    @Override
    public void visit(StatementVisitor visitor) {
        visitor.visitDeclareVar(this);
    }
}
