package mackycheese21.ferricoxide.ast.stmt;

import mackycheese21.ferricoxide.ast.visitor.StatementVisitor;

public abstract class Statement {

    public final boolean terminal;

    protected Statement(boolean terminal) {
        this.terminal = terminal;
    }

    public abstract <T> T visit(StatementVisitor<T> visitor);

}
