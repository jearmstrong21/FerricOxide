package mackycheese21.ferricoxide.ast.hl.stmt;

public interface HLStatementVisitor<T> {

    T visitAssign(HLAssign stmt);

    T visitBlock(HLBlock stmt);

    T visitBreak(HLBreak stmt);

    T visitCallStmt(HLCallStmt stmt);

    T visitDeclare(HLDeclare stmt);

    T visitFor(HLFor stmt);

    T visitIfStmt(HLIfStmt stmt);

    T visitLoop(HLLoop stmt);

    T visitReturn(HLReturn stmt);

    T visitWhile(HLWhile stmt);

}
