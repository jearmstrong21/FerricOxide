package mackycheese21.ferricoxide.ast.hl.stmt;

import mackycheese21.ferricoxide.ast.hl.expr.HLExpression;
import mackycheese21.ferricoxide.parser.token.Span;
import org.jetbrains.annotations.Nullable;

public class HLReturn extends HLStatement {

    public final @Nullable HLExpression value;

    public HLReturn(Span span, @Nullable HLExpression value) {
        super(span);
        this.value = value;
    }

    @Override
    public <T> T visit(HLStatementVisitor<T> visitor) {
        return visitor.visitReturn(this);
    }
}
