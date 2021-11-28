package mackycheese21.ferricoxide.ast.visitor;

import mackycheese21.ferricoxide.ast.stmt.*;

public interface StatementVisitor<T> {

    T visitAssign(Assign assign);

    T visitIfStmt(IfStmt ifStmt);

    T visitBlock(Block blockStmt);

    T visitReturnStmt(ReturnStmt returnStmt);

    T visitDeclareVar(DeclareVar declareVar);

    T visitWhileStmt(WhileStmt whileStmt);

    T visitCallStmt(CallStmt callStmt);

}
