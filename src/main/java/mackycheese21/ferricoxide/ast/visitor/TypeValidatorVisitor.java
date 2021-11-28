package mackycheese21.ferricoxide.ast.visitor;

import mackycheese21.ferricoxide.ast.ConcreteType;
import mackycheese21.ferricoxide.ast.IdentifierMap;
import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.CompilerException;
import mackycheese21.ferricoxide.ast.expr.*;
import mackycheese21.ferricoxide.ast.module.FOModule;
import mackycheese21.ferricoxide.ast.module.Function;
import mackycheese21.ferricoxide.ast.stmt.*;

public class TypeValidatorVisitor implements ExpressionVisitor<ConcreteType>, StatementVisitor<Void>, ModuleVisitor<Void> {

    private IdentifierMap<ConcreteType> variables;
    private IdentifierMap<ConcreteType.Function> functions;
    public ConcreteType requireReturnType;
    public boolean readOnly = false;

    public TypeValidatorVisitor(IdentifierMap<ConcreteType> variables, IdentifierMap<ConcreteType.Function> functions) {
        this.variables = variables;
        this.functions = functions;
    }

    public TypeValidatorVisitor() {

    }

    @Override
    public ConcreteType visitAccessVar(AccessVar accessVar) {
        return variables.mapGet(accessVar.name);
    }

    @Override
    public ConcreteType visitIntConstant(IntConstant intConstant) {
        return ConcreteType.I32;
    }

    @Override
    public ConcreteType visitBinaryExpr(BinaryExpr binaryExpr) {
        ConcreteType a = binaryExpr.a.visit(this);
        ConcreteType b = binaryExpr.b.visit(this);
        AnalysisException.requireType(a, b);
        return binaryExpr.operator.getResult(a);
    }

    @Override
    public ConcreteType visitUnaryExpr(UnaryExpr unaryExpr) {
        ConcreteType a = unaryExpr.a.visit(this);
        return unaryExpr.operator.getResult(a);
    }

    @Override
    public ConcreteType visitIfExpr(IfExpr ifExpr) {
        ConcreteType a = ifExpr.condition.visit(this);
        ConcreteType b = ifExpr.then.visit(this);
        ConcreteType c = ifExpr.otherwise.visit(this);
        AnalysisException.requireType(ConcreteType.BOOL, a);
        AnalysisException.requireType(b, c);
        return b;
    }

    @Override
    public ConcreteType visitBoolConstant(BoolConstant boolConstant) {
        return ConcreteType.BOOL;
    }

    @Override
    public ConcreteType visitCallExpr(CallExpr callExpr) {
        ConcreteType.Function type = functions.mapGet(callExpr.name);
        AnalysisException.requireParamCount(type.params.size(), callExpr.params.size());
        for (int i = 0; i < type.params.size(); i++) {
            AnalysisException.requireType(type.params.get(i), callExpr.params.get(i).visit(this));
        }
        return type.result;
    }

    @Override
    public Void visitAssign(Assign assign) {
        if (assign.a.lvalue) {
            ConcreteType a = assign.a.visit(this);
            ConcreteType b = assign.b.visit(this);
            AnalysisException.requireType(a, b);
            return null;
        } else {
            throw AnalysisException.cannotAssignValue();
        }
    }

    @Override
    public Void visitIfStmt(IfStmt ifStmt) {
        AnalysisException.requireType(ConcreteType.BOOL, ifStmt.condition.visit(this));
        ifStmt.condition.visit(this);
        if (ifStmt.otherwise != null) ifStmt.otherwise.visit(this);
        return null;
    }

    @Override
    public Void visitBlock(Block blockStmt) {
        blockStmt.statements.forEach(stmt -> stmt.visit(this));
        return null;
    }

    @Override
    public Void visitReturnStmt(ReturnStmt returnStmt) {
        AnalysisException.requireType(requireReturnType, returnStmt.value.visit(this));
        return null;
    }

    @Override
    public Void visitDeclareVar(DeclareVar declareVar) {
        if (readOnly) throw CompilerException.readOnlyTypeValidator();
        ConcreteType type = declareVar.value.visit(this);
        AnalysisException.requireType(declareVar.type, type);
        variables.mapAdd(declareVar.name, type);
        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStmt whileStmt) {
        AnalysisException.requireType(ConcreteType.BOOL, whileStmt.condition.visit(this));
        whileStmt.body.visit(this);
        return null;
    }

    @Override
    public Void visitCallStmt(CallStmt callStmt) {
        visitCallExpr(callStmt.callExpr);
        return null;
    }

    @Override
    public Void visit(FOModule module) {
        if (readOnly) throw CompilerException.readOnlyTypeValidator();
        functions = new IdentifierMap<>(null);
        for (Function function : module.functions) {
            if (function.inline && function.isExtern()) {
                throw AnalysisException.cannotCombineInlineExtern();
            }
            for (Function f2 : module.functions) {
                if (f2 != function && f2.name.equals(function.name)) {
                    throw AnalysisException.cannotOverloadFunction();
                }
            }
            functions.mapAdd(function.name, function.type);
        }
        for (Function function : module.functions) {
            if (function.isExtern()) continue;
            variables = new IdentifierMap<>(null);
            for (int i = 0; i < function.paramNames.size(); i++) {
                variables.mapAdd(function.paramNames.get(i), function.type.params.get(i));
            }
            requireReturnType = function.type.result;
            visitBlock(function.body);
            if (!function.body.terminal && function.type.result != ConcreteType.VOID)
                throw AnalysisException.functionMustHaveReturn();
            requireReturnType = null;
        }
        return null;
    }
}
