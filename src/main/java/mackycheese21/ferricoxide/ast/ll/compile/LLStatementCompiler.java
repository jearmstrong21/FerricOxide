package mackycheese21.ferricoxide.ast.ll.compile;

import mackycheese21.ferricoxide.ast.ll.stmt.*;
import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.List;

import static org.bytedeco.llvm.global.LLVM.*;

public class LLStatementCompiler implements LLStatementVisitor {

    private final LLVMBuilderRef builder;
    private final LLVMValueRef currentFunction;
    private final LLExpressionCompiler expressionCompiler;

    private LLVMBasicBlockRef loopBreakTarget;

    public LLStatementCompiler(LLVMBuilderRef builder, LLVMValueRef currentFunction, LLExpressionCompiler expressionCompiler) {
        this.builder = builder;
        this.currentFunction = currentFunction;
        this.expressionCompiler = expressionCompiler;
    }

    private void compileList(List<LLStatement> stmts) {
        stmts.forEach(s -> s.visit(this));
    }

    @Override
    public void visitAssign(LLAssign stmt) {
        LLVMValueRef left = stmt.left.visit(expressionCompiler);
        LLVMValueRef right = stmt.right.visit(expressionCompiler);
        LLVMBuildStore(builder, right, left);
    }

    @Override
    public void visitBreak(LLBreak stmt) {
        LLVMBuildBr(builder, loopBreakTarget);
    }

    @Override
    public void visitCallStmt(LLCallStmt stmt) {
        stmt.callExpr.visit(expressionCompiler);
    }

    @Override
    public void visitIfStmt(LLIfStmt stmt) {
        LLVMBasicBlockRef thenBlock = LLVMAppendBasicBlock(currentFunction, "then");
        LLVMBasicBlockRef otherwiseBlock = LLVMAppendBasicBlock(currentFunction, "otherwise");
        LLVMBasicBlockRef endBlock = LLVMAppendBasicBlock(currentFunction, "end");

        LLVMValueRef condition = stmt.condition.visit(expressionCompiler);
        LLVMBuildCondBr(builder, condition, thenBlock, otherwiseBlock);

        LLVMPositionBuilderAtEnd(builder, thenBlock);
        compileList(stmt.then);
        LLVMBuildBr(builder, endBlock);

        LLVMPositionBuilderAtEnd(builder, otherwiseBlock);
        compileList(stmt.otherwise);
        LLVMBuildBr(builder, otherwiseBlock);

        LLVMPositionBuilderAtEnd(builder, endBlock);
    }

    @Override
    public void visitLoop(LLLoop stmt) {
        LLVMBasicBlockRef start = LLVMAppendBasicBlock(currentFunction, "start");
        LLVMBasicBlockRef after = LLVMAppendBasicBlock(currentFunction, "after");

        LLVMBuildBr(builder, start);

        LLVMPositionBuilderAtEnd(builder, start);
        LLVMBasicBlockRef temp = loopBreakTarget;
        loopBreakTarget = after;
        compileList(stmt.statements);
        loopBreakTarget = temp;
        LLVMBuildBr(builder, start);

        LLVMPositionBuilderAtEnd(builder, after);
    }

    @Override
    public void visitReturn(LLReturn stmt) {
        if (stmt.value == null) LLVMBuildRetVoid(builder);
        else LLVMBuildRet(builder, stmt.value.visit(expressionCompiler));
    }
}
