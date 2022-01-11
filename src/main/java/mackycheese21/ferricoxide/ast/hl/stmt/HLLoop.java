package mackycheese21.ferricoxide.ast.hl.stmt;

import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;

public class HLLoop extends HLStatement {

    public final List<HLStatement> body;

    public HLLoop(Span span, List<HLStatement> body) {
        super(span);
        this.body = body;
    }

    @Override
    public <T> T visit(HLStatementVisitor<T> visitor) {
        return visitor.visitLoop(this);
    }
}
