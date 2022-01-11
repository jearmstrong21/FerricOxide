package mackycheese21.ferricoxide.ast.ll.stmt;

public interface LLStatementVisitor {

    void visitAssign(LLAssign stmt);

    void visitBreak(LLBreak stmt);

    void visitCallStmt(LLCallStmt stmt);

    void visitIfStmt(LLIfStmt stmt);

    void visitLoop(LLLoop stmt);

    void visitReturn(LLReturn stmt);

}
