package mackycheese21.ferricoxide.ast.hl.stmt;

import mackycheese21.ferricoxide.ast.hl.expr.HLExpression;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;

public class HLFor extends HLStatement {

    public final HLStatement init;
    public final HLExpression condition;
    public final HLStatement update;
    public final List<HLStatement> body;

    public HLFor(Span span, HLStatement init, HLExpression condition, HLStatement update, List<HLStatement> body) {
        super(span);
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }

    @Override
    public <T> T visit(HLStatementVisitor<T> visitor) {
        return visitor.visitFor(this);
    }
}
