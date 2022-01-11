package mackycheese21.ferricoxide.ast.hl.stmt;

import mackycheese21.ferricoxide.parser.token.Span;

public class HLBreak extends HLStatement {

    public HLBreak(Span span) {
        super(span);
    }

    @Override
    public <T> T visit(HLStatementVisitor<T> visitor) {
        return visitor.visitBreak(this);
    }
}
