package mackycheese21.ferricoxide.ast.stmt;

import mackycheese21.ferricoxide.ast.visitor.StatementVisitor;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;

public class Block extends Statement {

    public final List<Statement> statements;

    public Block(Span span, List<Statement> statements) {
        super(span, statements.stream().anyMatch(stmt -> stmt.terminal));
        this.statements = statements;
    }

    @Override
    public void visit(StatementVisitor visitor) {
        visitor.visitBlock(this);
    }

}
