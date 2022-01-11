package mackycheese21.ferricoxide.ast.hl.stmt;

import mackycheese21.ferricoxide.parser.token.Span;

public abstract class HLStatement {

    public final Span span;

    protected HLStatement(Span span) {
        this.span = span;
    }

    public abstract <T> T visit(HLStatementVisitor<T> visitor);


}
