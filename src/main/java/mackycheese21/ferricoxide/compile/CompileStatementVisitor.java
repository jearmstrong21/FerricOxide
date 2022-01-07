package mackycheese21.ferricoxide.compile;

import mackycheese21.ferricoxide.MapStack;
import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.stmt.*;
import mackycheese21.ferricoxide.ast.type.FOType;
import mackycheese21.ferricoxide.ast.visitor.StatementVisitor;
import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.Map;

import static org.bytedeco.llvm.global.LLVM.*;

public class CompileStatementVisitor implements StatementVisitor {

    private final LLVMBuilderRef builder;
    private final LLVMValueRef currentFunction;
    public final Map<DeclareVar, LLVMValueRef> allVariableRefs;
    public final MapStack<Identifier, FOType> variableTypes;
    public final MapStack<Identifier, LLVMValueRef> localVariableRefs;
    private final CompileExpressionVisitor compileExpression;

    public CompileStatementVisitor(LLVMBuilderRef builder,
                                   LLVMValueRef currentFunction,
                                   Map<DeclareVar, LLVMValueRef> allVariableRefs,
                                   Map<Identifier, FOType> globalTypes,
                                   Map<Identifier, LLVMValueRef> globalRefs,
                                   Map<Identifier, LLVMValueRef> functionRefs) {
        this.builder = builder;
        this.currentFunction = currentFunction;
        this.allVariableRefs = allVariableRefs;
        this.variableTypes = new MapStack<>();
        this.localVariableRefs = new MapStack<>();
        this.compileExpression = new CompileExpressionVisitor(
                builder,
                currentFunction,
                globalTypes,
                globalRefs,
                functionRefs,
                variableTypes,
                localVariableRefs
        );
    }

    @Override
    public void visitAssign(Assign assign) {
        LLVMBuildStore(builder, assign.b.visit(compileExpression), assign.a.visit(compileExpression));
    }

    @Override
    public void visitIfStmt(IfStmt ifStmt) {
        LLVMValueRef condition = ifStmt.condition.visit(compileExpression);
        if (ifStmt.otherwise == null) {
            LLVMBasicBlockRef then = LLVMAppendBasicBlock(currentFunction, "IfStmt.then");
            LLVMBasicBlockRef end = LLVMAppendBasicBlock(currentFunction, "IfStmt.end");
            LLVMBuildCondBr(builder, condition, then, end);
            LLVMPositionBuilderAtEnd(builder, then);
            visitBlock(ifStmt.then);
            if (!ifStmt.then.terminal) LLVMBuildBr(builder, end);
            LLVMPositionBuilderAtEnd(builder, end);
        } else {
            LLVMBasicBlockRef then = LLVMAppendBasicBlock(currentFunction, "IfStmt.then");
            LLVMBasicBlockRef otherwise = LLVMAppendBasicBlock(currentFunction, "IfStmt.otherwise");
            if (ifStmt.terminal) {
                LLVMBuildCondBr(builder, condition, then, otherwise);
                LLVMPositionBuilderAtEnd(builder, then);
                visitBlock(ifStmt.then);

                LLVMPositionBuilderAtEnd(builder, otherwise);
                visitBlock(ifStmt.otherwise);
            } else {
                LLVMBasicBlockRef end = LLVMAppendBasicBlock(currentFunction, "IfStmt.end");
                LLVMBuildCondBr(builder, condition, then, otherwise);
                LLVMPositionBuilderAtEnd(builder, then);
                visitBlock(ifStmt.then);
                if (!ifStmt.then.terminal) LLVMBuildBr(builder, end);
                LLVMPositionBuilderAtEnd(builder, otherwise);
                visitBlock(ifStmt.otherwise);
                if (!ifStmt.otherwise.terminal) LLVMBuildBr(builder, end);
                LLVMPositionBuilderAtEnd(builder, end);
            }
        }
    }

    @Override
    public void visitBlock(Block blockStmt) {
        variableTypes.push();
        localVariableRefs.push();
        blockStmt.statements.forEach(stmt -> stmt.visit(this));
        variableTypes.pop();
        localVariableRefs.pop();
    }

    @Override
    public void visitReturnStmt(ReturnStmt returnStmt) {
        if (returnStmt.value == null) LLVMBuildRetVoid(builder);
        else LLVMBuildRet(builder, returnStmt.value.visit(compileExpression));
    }

    @Override
    public void visitDeclareVar(DeclareVar declareVar) {
        if (!allVariableRefs.containsKey(declareVar)) throw new UnsupportedOperationException();
        LLVMValueRef valueRef = declareVar.value.visit(compileExpression);
        LLVMValueRef ptr = allVariableRefs.get(declareVar);
        LLVMBuildStore(builder, valueRef, ptr);
        variableTypes.put(declareVar.name, declareVar.type);
        localVariableRefs.put(declareVar.name, allVariableRefs.get(declareVar));
    }

    @Override
    public void visitWhileStmt(WhileStmt whileStmt) {
        LLVMBasicBlockRef cond = LLVMAppendBasicBlock(currentFunction, "cond");
        LLVMBasicBlockRef start = LLVMAppendBasicBlock(currentFunction, "start");
        LLVMBasicBlockRef end = LLVMAppendBasicBlock(currentFunction, "end");

        LLVMBuildBr(builder, cond);

        LLVMPositionBuilderAtEnd(builder, cond);
        LLVMBuildCondBr(builder, whileStmt.condition.visit(compileExpression), start, end);

        LLVMPositionBuilderAtEnd(builder, start);
        visitBlock(whileStmt.body);
        LLVMBuildBr(builder, cond);

        // TODO loops and terminal bodies

        LLVMPositionBuilderAtEnd(builder, end);
    }

    @Override
    public void visitForStmt(ForStmt forStmt) {
        forStmt.init.visit(this);

        LLVMBasicBlockRef cond = LLVMAppendBasicBlock(currentFunction, "cond");
        LLVMBasicBlockRef start = LLVMAppendBasicBlock(currentFunction, "start");
        LLVMBasicBlockRef end = LLVMAppendBasicBlock(currentFunction, "end");

        LLVMBuildBr(builder, cond);

        LLVMPositionBuilderAtEnd(builder, cond);
        LLVMBuildCondBr(builder, forStmt.condition.visit(compileExpression), start, end);

        LLVMPositionBuilderAtEnd(builder, start);
        visitBlock(forStmt.body);
        forStmt.update.visit(this);
        LLVMBuildBr(builder, cond);

        LLVMPositionBuilderAtEnd(builder, end);
    }

    @Override
    public void visitCallStmt(CallStmt callStmt) {
        callStmt.callExpr.visit(compileExpression);
    }
}
