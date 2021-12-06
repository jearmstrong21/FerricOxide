package mackycheese21.ferricoxide.ast.visitor;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.CompilerException;
import mackycheese21.ferricoxide.ast.IdentifierMap;
import mackycheese21.ferricoxide.ast.expr.*;
import mackycheese21.ferricoxide.ast.module.FOModule;
import mackycheese21.ferricoxide.ast.module.Function;
import mackycheese21.ferricoxide.ast.module.GlobalVariable;
import mackycheese21.ferricoxide.ast.stmt.*;
import mackycheese21.ferricoxide.ast.type.ConcreteType;
import mackycheese21.ferricoxide.ast.type.FunctionType;
import mackycheese21.ferricoxide.ast.type.PointerType;
import mackycheese21.ferricoxide.ast.type.StructType;

public class TypeValidatorVisitor implements ExpressionVisitor<ConcreteType>, StatementVisitor<Void>, ModuleVisitor<Void> {

    private IdentifierMap<ConcreteType> globals;
    private IdentifierMap<StructType> structs;
    private IdentifierMap<ConcreteType> variables;
    private IdentifierMap<FunctionType> functions;
    public ConcreteType requireReturnType;
    public boolean readOnly = false;

    public TypeValidatorVisitor(IdentifierMap<ConcreteType> globals, IdentifierMap<StructType> structs, IdentifierMap<ConcreteType> variables, IdentifierMap<FunctionType> functions) {
        this.globals = globals;
        this.structs = structs;
        this.variables = variables;
        this.functions = functions;
    }

    public TypeValidatorVisitor() {

    }

    private ConcreteType implicitResolve(ConcreteType expected, ConcreteType actual) {
        ConcreteType original = actual;
        while (actual instanceof PointerType pointer && expected != actual) {
            actual = pointer.to;
        }
        if (expected == actual) return actual;
        try {
            CastOperator.verify(actual, expected);
            return expected;
        } catch (AnalysisException e) {
            throw AnalysisException.incorrectImplicitResolveType(expected, actual, original);
        }
    }

    @Override
    public ConcreteType visitAccessVar(AccessVar accessVar) {
        if (globals.mapHas(accessVar.name)) return globals.mapGet(accessVar.name);
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
        implicitResolve(a, b);
        return binaryExpr.operator.getResult(a);
    }

    @Override
    public ConcreteType visitUnaryExpr(UnaryExpr unaryExpr) {
        return unaryExpr.operator.getResult(unaryExpr.a.visit(this));
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
        FunctionType type = functions.mapGet(callExpr.name);
        AnalysisException.requireParamCount(type.params.size(), callExpr.params.size());
        for (int i = 0; i < type.params.size(); i++) {
            implicitResolve(type.params.get(i), callExpr.params.get(i).visit(this));
        }
        return type.result;
    }

    @Override
    public ConcreteType visitAccessField(AccessField accessField) {
        PointerType objectType = AnalysisException.requirePointer(accessField.object.visit(this));
        ConcreteType fieldType = objectType.to.getFieldType(accessField.field);
        if (fieldType == null) throw AnalysisException.noSuchField(objectType, accessField.field);
        return fieldType;
    }

    @Override
    public ConcreteType visitStructInit(StructInit structInit) {
        StructType struct = structs.mapGet(structInit.struct);
        if (struct.fieldTypes.size() != structInit.fieldNames.size())
            throw AnalysisException.incorrectStructInitializer("expected %s fields, got %s".formatted(struct.fieldTypes.size(), structInit.fieldNames.size()));
        if (struct.fieldTypes.size() != structInit.fieldValues.size())
            throw AnalysisException.incorrectStructInitializer("expected %s fields, got %s".formatted(struct.fieldTypes.size(), structInit.fieldNames.size()));

        for (int i = 0; i < struct.fieldTypes.size(); i++) {
            if (!struct.fieldNames.contains(structInit.fieldNames.get(i)))
                throw AnalysisException.incorrectStructInitializer("field name %s not present in struct".formatted(structInit.fieldNames.get(i)));
            if (!structInit.fieldNames.contains(struct.fieldNames.get(i)))
                throw AnalysisException.incorrectStructInitializer("field name %s not present in init".formatted(struct.fieldNames.get(i)));
            String fieldName = structInit.fieldNames.get(i);
            Expression fieldValue = structInit.fieldValues.get(i);
            int actualStructIndex = struct.fieldNames.indexOf(fieldName);
            implicitResolve(struct.fieldTypes.get(actualStructIndex), fieldValue.visit(this));
//            AnalysisException.requireType(struct.fieldTypes.get(actualStructIndex), fieldValue.visit(this));
        }
        return struct;
    }

    @Override
    public ConcreteType visitPointerDeref(PointerDeref pointerDeref) {
        return AnalysisException.requirePointer(pointerDeref.deref.visit(this)).to;
    }

    @Override
    public ConcreteType visitCastExpr(CastExpr castExpr) {
        CastOperator.verify(castExpr.value.visit(this), castExpr.target);
        return castExpr.target;
    }

    @Override
    public ConcreteType visitAccessIndex(AccessIndex accessIndex) {
        AnalysisException.requireType(ConcreteType.I32, accessIndex.index.visit(this));
        return AnalysisException.requirePointer(accessIndex.value.visit(this)).to;
    }

    @Override
    public ConcreteType visitStringConstant(StringConstant stringConstant) {
        return PointerType.of(ConcreteType.I8);
    }

    @Override
    public ConcreteType visitRefAccessVar(RefAccessVar refAccessVar) {
        if (globals.mapHas(refAccessVar.name)) return PointerType.of(globals.mapGet(refAccessVar.name));
        return PointerType.of(variables.mapGet(refAccessVar.name));
    }

    @Override
    public ConcreteType visitRefAccessField(RefAccessField refAccessField) {
        PointerType objectType = AnalysisException.requirePointer(refAccessField.object.visit(this));
//        ConcreteType objectType = refAccessField.object.visit(this);
        ConcreteType fieldType = objectType.to.getFieldType(refAccessField.field);
        if (fieldType == null) throw AnalysisException.noSuchField(objectType.to, refAccessField.field);
        return PointerType.of(fieldType);
    }

    @Override
    public ConcreteType visitRefAccessIndex(RefAccessIndex refAccessIndex) {
        AnalysisException.requireType(ConcreteType.I32, refAccessIndex.index.visit(this));
        return AnalysisException.requirePointer(refAccessIndex.value.visit(this));
    }

    @Override
    public ConcreteType visitSizeOf(SizeOf sizeOf) {
        return ConcreteType.I32;
    }

    @Override
    public ConcreteType visitZeroInit(ZeroInit zeroInit) {
        return zeroInit.type;
    }

    @Override
    public Void visitAssign(Assign assign) {
        PointerType a = AnalysisException.requirePointer(assign.a.visit(this));
        ConcreteType b = assign.b.visit(this);
        AnalysisException.requireType(a.to, b);
        return null;
    }

    @Override
    public Void visitIfStmt(IfStmt ifStmt) {
        AnalysisException.requireType(ConcreteType.BOOL, ifStmt.condition.visit(this));
        visitBlock(ifStmt.then);
        if (ifStmt.otherwise != null) {
            visitBlock(ifStmt.otherwise);
        }
        return null;
    }

    @Override
    public Void visitBlock(Block blockStmt) {
        variables.push();
        blockStmt.statements.forEach(stmt -> stmt.visit(this));
        variables.pop();
        return null;
    }

    @Override
    public Void visitReturnStmt(ReturnStmt returnStmt) {
        if (requireReturnType == ConcreteType.VOID && returnStmt.value == null) return null;
        if (returnStmt.value == null) AnalysisException.requireType(requireReturnType, ConcreteType.VOID);
        else AnalysisException.requireType(requireReturnType, returnStmt.value.visit(this));
        return null;
    }

    @Override
    public Void visitDeclareVar(DeclareVar declareVar) {
        if (readOnly) throw CompilerException.readOnlyTypeValidator();
        ConcreteType type = declareVar.value.visit(this);
        if (!type.declarable) throw AnalysisException.cannotDeclareType(type);
        AnalysisException.requireType(declareVar.type, type);
        variables.mapAdd(declareVar.name, type);
        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStmt whileStmt) {
        AnalysisException.requireType(ConcreteType.BOOL, whileStmt.condition.visit(this));
        visitBlock(whileStmt.body);
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

        globals = new IdentifierMap<>(null);
        structs = new IdentifierMap<>(null);
        functions = new IdentifierMap<>(null);

        for (StructType struct : module.structs) {
            structs.mapAdd(struct.name, struct);
        }
        for (GlobalVariable global : module.globals) {
            globals.mapAdd(global.name, global.type);
        }
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
        this.variables = new IdentifierMap<>(null);
        for (GlobalVariable global : module.globals) {
            AnalysisException.requireType(global.type, global.value.visit(this));
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
