package mackycheese21.ferricoxide.ast.visitor;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.ast.IdentifierMap;
import mackycheese21.ferricoxide.ast.expr.*;
import mackycheese21.ferricoxide.ast.module.FOModule;
import mackycheese21.ferricoxide.ast.module.Function;
import mackycheese21.ferricoxide.ast.stmt.*;
import mackycheese21.ferricoxide.ast.type.*;

import java.util.stream.Collectors;

public class ResolveVisitor implements ExpressionVisitor<Void>, StatementVisitor<Void>, ModuleVisitor<Void> {

    private FOModule module;

    @Override
    public Void visit(FOModule module) {
        this.module = module;
        for (int i = 0; i < module.structs.size(); i++) {
            module.structs.set(i, fixStruct(module.structs.get(i)));
        }
        for (int i = 0; i < module.structs.size(); i++) {
            StructType struct = module.structs.get(i);
            for (int j = 0; j < struct.fieldTypes.size(); j++) {
                struct.fieldTypes.set(j, fix(struct.fieldTypes.get(j)));
            }
            struct.resolved();
        }
        for (int i = 0; i < module.globals.size(); i++) {
            module.globals.get(i).type = fix(module.globals.get(i).type);
            module.globals.get(i).value.visit(this);
        }
        for (int i = 0; i < module.functions.size(); i++) {
            Function function = module.functions.get(i);
            function.type = fixFunction(function.type);
            if (!function.isExtern()) {
                function.body.visit(this);
                if (!function.body.terminal) {
                    function.body.statements.add(new ReturnStmt(null));
                }
            }
        }
        return null;
    }

    private final IdentifierMap<StructType> fixedStructs = new IdentifierMap<>(null);

    private StructType fixStruct(StructType struct) {
        fixedStructs.mapAdd(struct.name, new StructType(struct.name, struct.fieldNames, struct.fieldTypes, struct.packed));
        return fixedStructs.mapGet(struct.name);
    }

    private FunctionType fixFunction(FunctionType function) {
        return FunctionType.of(fix(function.result), function.params.stream().map(this::fix).collect(Collectors.toList()));
    }

    private ConcreteType fix(ConcreteType type) {
        if (type instanceof StructType struct) return struct;
        if (type instanceof FunctionType function) return fixFunction(function);
        if (type instanceof PointerType pointer) return PointerType.of(fix(pointer.to));
        if (type instanceof TypeReference reference) {
            if (fixedStructs.mapHas(reference.name)) return fixedStructs.mapGet(reference.name);
            for (StructType s : module.structs) {
                if (s.name.equals(reference.name)) {
                    return fixStruct(s);
                }
            }
            throw AnalysisException.noTypeDeclared(reference);
        }
        if (!type.complete) throw new UnsupportedOperationException(type.getClass().toString());
        return type;
    }

    @Override
    public Void visitAssign(Assign assign) {
        assign.a.visit(this);
        assign.b.visit(this);
        return null;
    }

    @Override
    public Void visitIfStmt(IfStmt ifStmt) {
        ifStmt.condition.visit(this);
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
        if (returnStmt.value != null) returnStmt.value.visit(this);
        return null;
    }

    @Override
    public Void visitDeclareVar(DeclareVar declareVar) {
        declareVar.type = fix(declareVar.type);
        declareVar.value.visit(this);
        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStmt whileStmt) {
        whileStmt.condition.visit(this);
        whileStmt.body.visit(this);
        return null;
    }

    @Override
    public Void visitCallStmt(CallStmt callStmt) {
        callStmt.callExpr.visit(this);
        return null;
    }

    @Override
    public Void visitAccessVar(AccessVar accessVar) {
        return null;
    }

    @Override
    public Void visitIntConstant(IntConstant intConstant) {
        return null;
    }

    @Override
    public Void visitUnaryExpr(UnaryExpr unaryExpr) {
        unaryExpr.a.visit(this);
        return null;
    }

    @Override
    public Void visitBinaryExpr(BinaryExpr binaryExpr) {
        binaryExpr.a.visit(this);
        binaryExpr.b.visit(this);
        return null;
    }

    @Override
    public Void visitIfExpr(IfExpr ifExpr) {
        ifExpr.condition.visit(this);
        ifExpr.then.visit(this);
        ifExpr.otherwise.visit(this);
        return null;
    }

    @Override
    public Void visitBoolConstant(BoolConstant boolConstant) {
        return null;
    }

    @Override
    public Void visitCallExpr(CallExpr callExpr) {
        callExpr.params.forEach(e -> e.visit(this));
        return null;
    }

    @Override
    public Void visitAccessField(AccessField accessField) {
        accessField.object.visit(this);
        return null;
    }

    @Override
    public Void visitStructInit(StructInit structInit) {
        structInit.fieldValues.forEach(e -> e.visit(this));
        return null;
    }

    @Override
    public Void visitPointerDeref(PointerDeref pointerDeref) {
        pointerDeref.deref.visit(this);
        return null;
    }

    @Override
    public Void visitCastExpr(CastExpr castExpr) {
        castExpr.value.visit(this);
        castExpr.target = fix(castExpr.target);
        return null;
    }

    @Override
    public Void visitAccessIndex(AccessIndex accessIndex) {
        accessIndex.value.visit(this);
        accessIndex.index.visit(this);
        return null;
    }

    @Override
    public Void visitStringConstant(StringConstant stringConstant) {
        return null;
    }

    @Override
    public Void visitRefAccessVar(RefAccessVar refAccessVar) {
        return null;
    }

    @Override
    public Void visitRefAccessField(RefAccessField refAccessField) {
        refAccessField.object.visit(this);
        return null;
    }

    @Override
    public Void visitRefAccessIndex(RefAccessIndex refAccessIndex) {
        refAccessIndex.value.visit(this);
        refAccessIndex.index.visit(this);
        return null;
    }

    @Override
    public Void visitSizeOf(SizeOf sizeOf) {
        sizeOf.type = fix(sizeOf.type);
        return null;
    }

    @Override
    public Void visitZeroInit(ZeroInit zeroInit) {
        zeroInit.type = fix(zeroInit.type);
        return null;
    }
}
