package mackycheese21.ferricoxide.ast.visitor;

import mackycheese21.ferricoxide.ast.stmt.*;

public interface StatementVisitor {

    void visitForStmt(ForStmt forStmt);

    void visitAssign(Assign assign);

    void visitIfStmt(IfStmt ifStmt);

    void visitBlock(Block blockStmt);

    void visitReturnStmt(ReturnStmt returnStmt);

    void visitDeclareVar(DeclareVar declareVar);

    void visitWhileStmt(WhileStmt whileStmt);

    void visitCallStmt(CallStmt callStmt);

}
