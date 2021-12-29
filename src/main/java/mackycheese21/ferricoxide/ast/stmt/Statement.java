package mackycheese21.ferricoxide.ast.stmt;

import mackycheese21.ferricoxide.ast.visitor.StatementVisitor;
import mackycheese21.ferricoxide.parser.token.Span;

public abstract class Statement {

    public final Span span;
    public final boolean terminal;

    protected Statement(Span span, boolean terminal) {
        this.span = span;
        this.terminal = terminal;
    }

    public abstract <T> T visit(StatementVisitor<T> visitor);

}
