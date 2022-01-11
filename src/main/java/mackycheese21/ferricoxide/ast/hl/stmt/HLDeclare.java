package mackycheese21.ferricoxide.ast.hl.stmt;

import mackycheese21.ferricoxide.ast.hl.expr.HLExpression;
import mackycheese21.ferricoxide.ast.hl.type.HLType;
import mackycheese21.ferricoxide.parser.token.Span;
import org.jetbrains.annotations.Nullable;

public class HLDeclare extends HLStatement {

    public final String name;
    public final @Nullable HLType type;
    public final HLExpression value;

    public HLDeclare(Span span, String name, @Nullable HLType type, HLExpression value) {
        super(span);
        this.name = name;
        this.type = type;
        this.value = value;
    }

    @Override
    public <T> T visit(HLStatementVisitor<T> visitor) {
        return visitor.visitDeclare(this);
    }
}
