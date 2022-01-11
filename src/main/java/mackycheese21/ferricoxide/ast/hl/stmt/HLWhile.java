package mackycheese21.ferricoxide.ast.hl.stmt;

import mackycheese21.ferricoxide.ast.hl.expr.HLExpression;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;

public class HLWhile extends HLStatement {

    public final HLExpression condition;
    public final List<HLStatement> body;

    public HLWhile(Span span, HLExpression condition, List<HLStatement> body) {
        super(span);
        this.condition = condition;
        this.body = body;
    }

    @Override
    public <T> T visit(HLStatementVisitor<T> visitor) {
        return visitor.visitWhile(this);
    }
}
