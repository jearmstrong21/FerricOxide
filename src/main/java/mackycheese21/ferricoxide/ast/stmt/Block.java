package mackycheese21.ferricoxide.ast.stmt;

import mackycheese21.ferricoxide.ast.visitor.StatementVisitor;

import java.util.Collections;
import java.util.List;

public class Block extends Statement {

    public final List<Statement> statements;

    public Block(List<Statement> statements) {
        super(statements.stream().anyMatch(stmt -> stmt.terminal));
        this.statements = Collections.unmodifiableList(statements);
    }

    @Override
    public <T> T visit(StatementVisitor<T> visitor) {
        return visitor.visitBlock(this);
    }

}
