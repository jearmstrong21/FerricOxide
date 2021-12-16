package mackycheese21.ferricoxide.compile;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.IdentifierMap;
import mackycheese21.ferricoxide.ast.stmt.*;
import mackycheese21.ferricoxide.ast.type.ConcreteType;
import mackycheese21.ferricoxide.ast.type.PointerType;
import mackycheese21.ferricoxide.ast.type.StructType;
import mackycheese21.ferricoxide.ast.visitor.StatementVisitor;
import mackycheese21.ferricoxide.ast.visitor.TypeValidatorVisitor;
import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.Map;

import static org.bytedeco.llvm.global.LLVM.*;

public class CompileStatementVisitor implements StatementVisitor<Void> {

    private final LLVMBuilderRef builder;
    private final LLVMValueRef currentFunction;
    public final Map<DeclareVar, LLVMValueRef> allVariableRefs;
    private final IdentifierMap<ConcreteType> globalTypes;
    private final IdentifierMap<LLVMValueRef> globalRefs;
    public final IdentifierMap<ConcreteType> variableTypes;
    public final IdentifierMap<LLVMValueRef> localVariableRefs;
    private final CompileExpressionVisitor compileExpression;
    private final TypeValidatorVisitor typeValidator;

    public CompileStatementVisitor(LLVMBuilderRef builder,
                                   LLVMValueRef currentFunction,
                                   Map<DeclareVar, LLVMValueRef> allVariableRefs,
                                   IdentifierMap<ConcreteType> globalTypes,
                                   IdentifierMap<LLVMValueRef> globalRefs,
                                   Map<String, LLVMValueRef> strings,
                                   IdentifierMap<StructType> structs) {
        this.builder = builder;
        this.currentFunction = currentFunction;
        this.allVariableRefs = allVariableRefs;
        this.globalTypes = globalTypes;
        this.globalRefs = globalRefs;
        this.variableTypes = new IdentifierMap<>(null);
        this.localVariableRefs = new IdentifierMap<>(null);
        this.compileExpression = new CompileExpressionVisitor(builder, currentFunction, strings, globalTypes, globalRefs, structs, variableTypes, localVariableRefs);
        this.typeValidator = new TypeValidatorVisitor(globalTypes, structs, variableTypes);
    }

    @Override
    public Void visitAssign(Assign assign) {
        if (assign.a.visit(typeValidator) instanceof PointerType pointer) {
            LLVMBuildStore(builder, assign.b.visit(compileExpression), assign.a.visit(compileExpression));
            return null;
        } else {
            throw new UnsupportedOperationException("wat");
        }
    }

    @Override
    public Void visitIfStmt(IfStmt ifStmt) {
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
        return null;
    }

    @Override
    public Void visitBlock(Block blockStmt) {
        variableTypes.push();
        localVariableRefs.push();
        blockStmt.statements.forEach(stmt -> stmt.visit(this));
        variableTypes.pop();
        localVariableRefs.pop();
        return null;
    }

    @Override
    public Void visitReturnStmt(ReturnStmt returnStmt) {
        if (returnStmt.value == null) LLVMBuildRetVoid(builder);
        else LLVMBuildRet(builder, returnStmt.value.visit(compileExpression));
        return null;
    }

    @Override
    public Void visitDeclareVar(DeclareVar declareVar) {
        if(!allVariableRefs.containsKey(declareVar)) throw AnalysisException.noSuchKey(new Identifier(declareVar.name, false));
        LLVMBuildStore(builder, declareVar.value.visit(compileExpression), allVariableRefs.get(declareVar));
        variableTypes.mapAdd(new Identifier(declareVar.name, false), declareVar.type);
        localVariableRefs.mapAdd(new Identifier(declareVar.name, false), allVariableRefs.get(declareVar));
        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStmt whileStmt) {
        LLVMBasicBlockRef cond = LLVMAppendBasicBlock(currentFunction, "WhileStmt.cond");
        LLVMBasicBlockRef start = LLVMAppendBasicBlock(currentFunction, "WhileStmt.start");
        LLVMBasicBlockRef end = LLVMAppendBasicBlock(currentFunction, "WhileStmt.end");

        LLVMBuildBr(builder, cond);

        LLVMPositionBuilderAtEnd(builder, cond);
        LLVMBuildCondBr(builder, whileStmt.condition.visit(compileExpression), start, end);

        LLVMPositionBuilderAtEnd(builder, start);
        visitBlock(whileStmt.body);
        if(!whileStmt.body.terminal) LLVMBuildBr(builder, cond);

        LLVMPositionBuilderAtEnd(builder, end);
        return null;
    }

    @Override
    public Void visitCallStmt(CallStmt callStmt) {
        callStmt.callExpr.visit(compileExpression);
        return null;
    }
}
