package mackycheese21.ferricoxide.ast.visitor;

import mackycheese21.ferricoxide.ast.IdentifierMap;
import mackycheese21.ferricoxide.ast.stmt.*;
import mackycheese21.ferricoxide.ast.type.ConcreteType;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;
import static org.bytedeco.llvm.global.LLVM.*;

import java.util.HashMap;
import java.util.Map;

public class AllDeclaredVariablesVisitor implements StatementVisitor<Void> {

    public final Map<DeclareVar, LLVMValueRef> variableRefs = new HashMap<>();
    private final LLVMBuilderRef builder;

    public AllDeclaredVariablesVisitor(LLVMBuilderRef builder) {
        this.builder = builder;
    }

    @Override
    public Void visitAssign(Assign assign) {
        return null;
    }

    @Override
    public Void visitIfStmt(IfStmt ifStmt) {
        ifStmt.then.visit(this);
        if(ifStmt.otherwise != null) ifStmt.otherwise.visit(this);
        return null;
    }

    @Override
    public Void visitBlock(Block blockStmt) {
        blockStmt.statements.forEach(s -> s.visit(this));
        return null;
    }

    @Override
    public Void visitReturnStmt(ReturnStmt returnStmt) {
        return null;
    }

    @Override
    public Void visitDeclareVar(DeclareVar declareVar) {
        variableRefs.put(declareVar, LLVMBuildAlloca(builder, declareVar.type.typeRef, declareVar.name));
        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStmt whileStmt) {
        whileStmt.body.visit(this);
        return null;
    }

    @Override
    public Void visitCallStmt(CallStmt callStmt) {
        return null;
    }
}
