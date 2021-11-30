package mackycheese21.ferricoxide.ast.visitor;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.ast.IdentifierMap;
import mackycheese21.ferricoxide.ast.module.FOModule;
import mackycheese21.ferricoxide.ast.module.Function;
import mackycheese21.ferricoxide.ast.stmt.*;
import mackycheese21.ferricoxide.ast.type.*;

import java.util.stream.Collectors;

public class ResolveVisitor implements StatementVisitor<Void>, ModuleVisitor<Void> {

    private FOModule module;

    @Override
    public Void visit(FOModule module) {
        this.module = module;
        for (int i = 0; i < module.structs.size(); i++) {
            module.structs.set(i, fixStruct(module.structs.get(i)));
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
        fixedStructs.mapAdd(struct.name, new StructType(struct.name, struct.fieldNames, struct.fieldTypes.stream().map(this::fix).collect(Collectors.toList()), struct.packed));
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
        declareVar.type = fix(declareVar.type);
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
