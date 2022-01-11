package mackycheese21.ferricoxide.ast.hl.stmt;

import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;

public class HLBlock extends HLStatement {

    public final List<HLStatement> statements;

    public HLBlock(Span span, List<HLStatement> statements) {
        super(span);
        this.statements = statements;
    }

    @Override
    public <T> T visit(HLStatementVisitor<T> visitor) {
        return visitor.visitBlock(this);
    }
}
