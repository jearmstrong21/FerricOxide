package mackycheese21.ferricoxide.compile;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.MapStack;
import mackycheese21.ferricoxide.Utils;
import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.expr.BinaryOperator;
import mackycheese21.ferricoxide.ast.expr.CallExpr;
import mackycheese21.ferricoxide.ast.module.FOModule;
import mackycheese21.ferricoxide.ast.module.Function;
import mackycheese21.ferricoxide.ast.module.GlobalVariable;
import mackycheese21.ferricoxide.ast.stmt.*;
import mackycheese21.ferricoxide.ast.type.*;
import mackycheese21.ferricoxide.ast.visitor.ModuleVisitor;
import mackycheese21.ferricoxide.ast.visitor.StatementVisitor;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TypeValidatorVisitor implements ModuleVisitor<Void>, StatementVisitor<Void> {

    private FOModule module = null;

    private final Map<Identifier, StructType> resolvedStructs = new HashMap<>();

    private FunctionType resolveFunction(FunctionType function) {
        return new FunctionType(
                resolve(function.result),
                function.params.stream().map(this::resolve).collect(Collectors.toList())
        );
    }

    private FOType resolve(FOType type) {
        if (type instanceof FunctionType function) return resolveFunction(function);
        if (type instanceof PointerType pointer) return new PointerType(resolve(pointer.to));
        if (type instanceof PrimitiveType) return type;
        // a struct is never returned by parser
        // and a struct returned by resolve is already fully resolved
        // :eggplant:
        if (type instanceof StructType struct) return struct;
        if (type instanceof TupleType tuple)
            return new TupleType(tuple.types.stream().map(this::resolve).collect(Collectors.toList()));
        if (type instanceof UnresolvedType unresolved) {
            if (resolvedStructs.containsKey(unresolved.identifier)) return resolvedStructs.get(unresolved.identifier);

            throw new AnalysisException(unresolved.identifier.span, "struct %s not found".formatted(unresolved.identifier));
        }
        throw new UnsupportedOperationException();
    }

    /**
     * Resolves the entire module
     * Adds type data to expression nodes
     * Resolves implicit casts
     */
    @Override
    public Void visit(FOModule module) {
        if (this.module != null) throw new UnsupportedOperationException("piss off, this is a single-use class");
        this.module = module;

        resolveStructs();

        checkGlobals();

        //// Functions
        for (Function function : module.functions()) {
            visitFunction(function);
        }

        return null;
    }

    private void resolveStructs() {
        for (StructType struct : module.structs()) {
            resolvedStructs.put(struct.identifier, struct);
        }
        for (StructType struct : module.structs()) {
            struct.fields.replaceAll((__, type) -> resolve(type));
        }
        for (GlobalVariable global : module.globals()) {
            global.type = resolve(global.type);
        }
        for (Function function : module.functions()) {
            function.type = resolveFunction(function.type);
        }
    }

    private void initializeFreshContext(Map<Identifier, FunctionType> validatorFunctions, Map<Identifier, FOType> validatorGlobals, Map<Identifier, StructType> validatorStructs) {
        for (Function f : module.functions()) {
            validatorFunctions.put(f.name, f.type);
        }
        for (GlobalVariable g : module.globals()) {
            validatorGlobals.put(g.name, g.type);
        }
        for (StructType s : module.structs()) {
            validatorStructs.put(s.identifier, s);
        }
    }

    private void checkGlobals() {
        Map<Identifier, FunctionType> validatorFunctions = new HashMap<>();
        Map<Identifier, FOType> validatorGlobals = new HashMap<>();
        Map<Identifier, StructType> validatorStructs = new HashMap<>();
        initializeFreshContext(validatorFunctions, validatorGlobals, validatorStructs);
        for (GlobalVariable global : module.globals()) {
            localVariables = new MapStack<>();
            expressionValidator = new ExpressionValidator(localVariables, validatorFunctions, validatorGlobals, global.name.removeLast(), validatorStructs, this::resolve);
            global.type = resolve(global.type);
            global.value = global.value.request(expressionValidator, global.type);
            Utils.requireType(global.type, global.value);
        }
    }

    private FOType expectedReturnType;
    private MapStack<Identifier, FOType> localVariables;
    private ExpressionValidator expressionValidator;

    private void visitFunction(Function function) {
        Map<Identifier, FunctionType> validatorFunctions = new HashMap<>();
        Map<Identifier, FOType> validatorGlobals = new HashMap<>();
        Map<Identifier, StructType> validatorStructs = new HashMap<>();
        initializeFreshContext(validatorFunctions, validatorGlobals, validatorStructs);
        function.type = resolveFunction(function.type);
        if (!function.isExtern()) {
            localVariables = new MapStack<>();
            expressionValidator = new ExpressionValidator(localVariables, validatorFunctions, validatorGlobals, function.name.removeLast(), validatorStructs, this::resolve);
            localVariables.push();
            for (int i = 0; i < function.paramNames.size(); i++) {
                localVariables.put(function.paramNames.get(i), function.type.params.get(i));
            }
            if (!function.body.terminal) {
                if (function.type.result != FOType.VOID) {
                    throw new AnalysisException(function.name.span, "function body must be terminal");
                } else {
                    function.implicitVoidReturn = true;
                }
            }

            expectedReturnType = function.type.result;
            visitBlock(function.body);
        }
    }

    @Override
    public Void visitForStmt(ForStmt forStmt) {
        forStmt.init.visit(this);
        forStmt.condition = forStmt.condition.request(expressionValidator, FOType.BOOL).implicitTo(FOType.BOOL);
        Utils.requireType(FOType.BOOL, forStmt.condition);
        visitBlock(forStmt.body);
        forStmt.update.visit(this);
        return null;
    }

    @Override
    public Void visitAssign(Assign assign) {
        if (assign.operator != BinaryOperator.DISCARD_FIRST)
            throw new AnalysisException(assign.span, "Only DISCARD_FIRST is supported in assignmments currently");
        assign.b = assign.b.request(expressionValidator, null);
        assign.a = assign.a.request(expressionValidator, new PointerType(assign.b.result)).implicitTo(new PointerType(assign.b.result)); // TODO request reference?
        Utils.requireType(new PointerType(assign.b.result), assign.a);
        return null;
    }

    @Override
    public Void visitIfStmt(IfStmt ifStmt) {
        ifStmt.condition = ifStmt.condition.request(expressionValidator, FOType.BOOL).implicitTo(FOType.BOOL);
        Utils.requireType(FOType.BOOL, ifStmt.condition);
        visitBlock(ifStmt.then);
        if (ifStmt.otherwise != null) {
            visitBlock(ifStmt.otherwise);
        }
        return null;
    }

    @Override
    public Void visitBlock(Block blockStmt) {
        localVariables.push();
        blockStmt.statements.forEach(stmt -> stmt.visit(this));
        localVariables.pop();
        return null;
    }

    @Override
    public Void visitReturnStmt(ReturnStmt returnStmt) {
        if (returnStmt.value == null) Utils.requireType(returnStmt.span, expectedReturnType, FOType.VOID);
        else {
            returnStmt.value = returnStmt.value.request(expressionValidator, expectedReturnType).implicitTo(expectedReturnType);
            Utils.requireType(expectedReturnType, returnStmt.value);
        }
        return null;
    }

    @Override
    public Void visitDeclareVar(DeclareVar declareVar) {
        declareVar.type = resolve(declareVar.type);
        localVariables.put(declareVar.name, declareVar.type);
        declareVar.value = declareVar.value.request(expressionValidator, declareVar.type).implicitTo(declareVar.type);
        Utils.requireType(declareVar.type, declareVar.value);
        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStmt whileStmt) {
        whileStmt.condition = whileStmt.condition.request(expressionValidator, FOType.BOOL).implicitTo(FOType.BOOL);
        Utils.requireType(FOType.BOOL, whileStmt.condition);
        visitBlock(whileStmt.body);
        return null;
    }

    @Override
    public Void visitCallStmt(CallStmt callStmt) {
        // this is okay because CallExpr is either validated or failed, never modified at the root
        callStmt.callExpr = (CallExpr) callStmt.callExpr.request(expressionValidator, null);
        return null;
    }
}
