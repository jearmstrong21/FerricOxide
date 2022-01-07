package mackycheese21.ferricoxide.compile;

import mackycheese21.ferricoxide.ast.stmt.*;
import mackycheese21.ferricoxide.ast.type.TypeRegistry;
import mackycheese21.ferricoxide.ast.visitor.StatementVisitor;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.llvm.global.LLVM.LLVMBuildAlloca;

public class AllDeclaredVariablesVisitor implements StatementVisitor {

    public final Map<DeclareVar, LLVMValueRef> variableRefs = new HashMap<>();
    private final LLVMBuilderRef builder;

    public AllDeclaredVariablesVisitor(LLVMBuilderRef builder) {
        this.builder = builder;
    }

    @Override
    public void visitForStmt(ForStmt forStmt) {
        forStmt.init.visit(this);
        forStmt.update.visit(this);
        forStmt.body.visit(this);
    }

    @Override
    public void visitAssign(Assign assign) {
    }

    @Override
    public void visitIfStmt(IfStmt ifStmt) {
        ifStmt.then.visit(this);
        if (ifStmt.otherwise != null) ifStmt.otherwise.visit(this);
    }

    @Override
    public void visitBlock(Block blockStmt) {
        blockStmt.statements.forEach(s -> s.visit(this));
    }

    @Override
    public void visitReturnStmt(ReturnStmt returnStmt) {
    }

    @Override
    public void visitDeclareVar(DeclareVar declareVar) {
        variableRefs.put(declareVar, LLVMBuildAlloca(builder, TypeRegistry.forceLookup(declareVar.type), declareVar.name.toString()));
    }

    @Override
    public void visitWhileStmt(WhileStmt whileStmt) {
        whileStmt.body.visit(this);
    }

    @Override
    public void visitCallStmt(CallStmt callStmt) {
    }
}
