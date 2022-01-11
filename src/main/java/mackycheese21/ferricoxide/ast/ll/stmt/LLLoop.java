package mackycheese21.ferricoxide.ast.ll.stmt;

import java.util.List;

public class LLLoop extends LLStatement {

    public final List<LLStatement> statements;

    public LLLoop(List<LLStatement> statements) {
        this.statements = statements;
    }

    @Override
    public void visit(LLStatementVisitor visitor) {
        visitor.visitLoop(this);
    }
}
