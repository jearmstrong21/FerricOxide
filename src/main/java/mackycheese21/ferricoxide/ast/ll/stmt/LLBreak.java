package mackycheese21.ferricoxide.ast.ll.stmt;

public class LLBreak extends LLStatement {

    public LLBreak() {

    }

    @Override
    public void visit(LLStatementVisitor visitor) {
        visitor.visitBreak(this);
    }
}
