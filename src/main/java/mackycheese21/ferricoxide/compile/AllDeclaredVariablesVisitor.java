package mackycheese21.ferricoxide.compile;

import mackycheese21.ferricoxide.ast.stmt.*;
import mackycheese21.ferricoxide.ast.type.TypeRegistry;
import mackycheese21.ferricoxide.ast.visitor.StatementVisitor;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.llvm.global.LLVM.LLVMBuildAlloca;

public class AllDeclaredVariablesVisitor implements StatementVisitor<Void> {

    public final Map<DeclareVar, LLVMValueRef> variableRefs = new HashMap<>();
    private final LLVMBuilderRef builder;

    public AllDeclaredVariablesVisitor(LLVMBuilderRef builder) {
        this.builder = builder;
    }

    @Override
    public Void visitForStmt(ForStmt forStmt) {
        forStmt.init.visit(this);
        forStmt.update.visit(this);
        forStmt.body.visit(this);
        return null;
    }

    @Override
    public Void visitAssign(Assign assign) {
        return null;
    }

    @Override
    public Void visitIfStmt(IfStmt ifStmt) {
        ifStmt.then.visit(this);
        if (ifStmt.otherwise != null) ifStmt.otherwise.visit(this);
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
        variableRefs.put(declareVar, LLVMBuildAlloca(builder, TypeRegistry.forceLookup(declareVar.type), declareVar.name.toString()));
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
