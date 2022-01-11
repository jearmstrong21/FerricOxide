package mackycheese21.ferricoxide.ast.hl.stmt;

import mackycheese21.ferricoxide.ast.hl.expr.HLExpression;
import mackycheese21.ferricoxide.parser.token.Span;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HLIfStmt extends HLStatement {

    public final HLExpression condition;
    public final List<HLStatement> then;
    public final @Nullable List<HLStatement> otherwise;

    public HLIfStmt(Span span, HLExpression condition, List<HLStatement> then, @Nullable List<HLStatement> otherwise) {
        super(span);
        this.condition = condition;
        this.then = then;
        this.otherwise = otherwise;
    }

    @Override
    public <T> T visit(HLStatementVisitor<T> visitor) {
        return visitor.visitIfStmt(this);
    }
}
