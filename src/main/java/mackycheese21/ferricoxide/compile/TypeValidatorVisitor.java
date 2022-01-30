//package mackycheese21.ferricoxide.compile;
//
//import mackycheese21.ferricoxide.AnalysisException;
//import mackycheese21.ferricoxide.MapStack;
//import mackycheese21.ferricoxide.Utils;
//import mackycheese21.ferricoxide.Identifier;
//import mackycheese21.ferricoxide.ast.expr.BinaryOperator;
//import mackycheese21.ferricoxide.ast.expr.CallExpr;
//import mackycheese21.ferricoxide.ast.module.*;
//import mackycheese21.ferricoxide.ast.stmt.*;
//import mackycheese21.ferricoxide.ast.type.*;
//import mackycheese21.ferricoxide.ast.visitor.ModuleVisitor;
//import mackycheese21.ferricoxide.ast.visitor.ResolutionContext;
//import mackycheese21.ferricoxide.ast.visitor.StatementVisitor;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//public class TypeValidatorVisitor implements ModuleVisitor<Void>, StatementVisitor, ResolutionContext {
//
//    private FOModule module = null;
//
//    private final Map<Identifier, StructType> resolvedStructs = new HashMap<>();
//    private final Map<Identifier, TraitType> resolvedTraits = new HashMap<>();
//
//    public FunctionType resolveFunction(FunctionType function) {
//        return new FunctionType(
//                resolveType(function.result),
//                function.params.stream().map(this::resolveType).collect(Collectors.toList())
//        );
//    }
//
//    @Override
//    public void setCurrentModPath(Identifier identifier) {
//        this.currentModPath = identifier;
//    }
//
//    @Override
//    public FOType resolveType(FOType type) {
//        if (type instanceof FunctionType function) resolveFunction(function);
//        if (type instanceof PointerType pointer) return new PointerType(resolveType(pointer.to));
//        if (type instanceof PrimitiveType) return type;
//        // a struct is never returned by parser
//        // and a struct returned by resolve is already fully resolved
//        // :eggplant:
//        if (type instanceof StructType struct) return struct;
//        if (type instanceof TupleType tuple)
//            return new TupleType(tuple.types.stream().map(this::resolveType).collect(Collectors.toList()));
//        if (type instanceof UnresolvedType unresolved) {
//            if (resolvedStructs.containsKey(Identifier.concat(currentModPath, unresolved.identifier)))
//                return resolvedStructs.get(Identifier.concat(currentModPath, unresolved.identifier));
//            if (resolvedStructs.containsKey(unresolved.identifier)) return resolvedStructs.get(unresolved.identifier);
//            throw new AnalysisException(unresolved.identifier.span, "struct %s not found".formatted(unresolved.identifier));
//        }
//        if (type instanceof UnresolvedImplTrait unresolved) {
//            if(resolvedTraits.containsKey(Identifier.concat(currentModPath, unresolved.identifier)))
//                return resolvedTraits.get(Identifier.concat(currentModPath, unresolved.identifier));
//            if(resolvedTraits.containsKey(unresolved.identifier)) return resolvedTraits.get(unresolved.identifier);
//            throw new AnalysisException(unresolved.identifier.span, "trait %s not found".formatted(unresolved.identifier));
//        }
//        if(type instanceof TraitType trait) return trait;
//        throw new UnsupportedOperationException(type.toString());
//    }
//
//    /**
//     * Resolves the entire module
//     * Adds type data to expression nodes
//     * Resolves implicit casts
//     */
//    @Override
//    public Void visit(FOModule module) {
//        if (this.module != null) throw new UnsupportedOperationException("piss off, this is a single-use class");
//        this.module = module;
//
//        resolveTraits1();
//
//        resolveStructs();
//
//        resolveTraits2();
//
//        resolveFunctions();
//
//        resolveGlobals();
//
//        //// Functions
//        for (Function function : module.functions) {
//            visitFunction(function);
//        }
//
//        return null;
//    }
//
//    private void initializeFreshContext(Map<Identifier, FunctionType> validatorFunctions, Map<Identifier, FOType> validatorGlobals, Map<Identifier, StructType> validatorStructs) {
//        for (Function f : module.functions) {
//            validatorFunctions.put(f.name, f.type);
//        }
//        for (GlobalVariable g : module.globals) {
//            validatorGlobals.put(g.name, g.type);
//        }
//        for (StructType s : module.structs) {
//            validatorStructs.put(s.identifier, s);
//        }
//    }
//
//    private void resolveStructs() {
//        for (StructType struct : module.structs) {
//            resolvedStructs.put(struct.identifier, struct);
//        }
//        for (StructType struct : module.structs) {
//            struct.fields.replaceAll((__, type) -> resolveType(type));
//        }
//    }
//
//    private void resolveTraits1() {
//        for(Trait trait : module.traits) {
//            resolvedTraits.put(trait.name, new TraitType(trait));
//        }
//    }
//
//    private void resolveTraits2() {
//        for(TraitType traitType : resolvedTraits.values()) {
//            for(FunctionPrototype fp : traitType.trait.methods) {
//                fp.result = resolveType(fp.result);
//                fp.paramTypes = fp.paramTypes.stream().map(this::resolveType).collect(Collectors.toList());
//                traitType.fields.put(FOType.Access.string(fp.name), new FunctionType());
//                // TODO: high level ast and low level ast
//                /*
//                hlast = direct output from parser
//                llast = no structs or traits, final llvm names, variables / functions fully and accurately specified, etc, basically a high-level LLVM ast that is very easily compiled
//                    - multiple things need to be done:
//                        - type resolution
//                        - variable / static dispatch resolution
//                        - dynamic dispatch resolution (WRITE OUT THE FORMAT FOR THIS IN MARKDOWN SOMEWHERE AND FIGURE OUT COMBINED TRAIT IMPLS AND HOW RUST DOES IT
//                        - implicit casts
//                goal: analysis done in the parser (static method, &self type inference, etc) be done in separated code
//                RUST &dyn T vs impl T
//                impl T shouldn't hide what it does imo
//                lang will eventually have impl T with just straight-up generics, makes for large code size that isn't immediately clear (generics are sufficient to carry the contract of large code size, but not impl T)
//                for now FO impl T <=> Rust &dyn T I think
//                reread llvm
//                TODO do hl/ll ast rewrite without traits first but plan for traits (just remove the impl ... for from test_lang_rewrite.fo and the parser just wont generate them)
//
//                having two same types is very confusing
//                also remove request and add implicit deref to implicitTo
//                 */
//            }
//        }
//    }
//
//    private void resolveFunctions() {
//        for (Function function : module.functions) {
//            currentModPath = function.getModPath();
//            function.type = resolveFunction(function.type);
//            if (function.enclosingType != null) {
//                function.enclosingType = resolveType(function.enclosingType);
//                function.enclosingType.methods.put(function.name.getLast(), function);
//            }
//        }
//    }
//
//    private void resolveGlobals() {
//        Map<Identifier, FunctionType> validatorFunctions = new HashMap<>();
//        Map<Identifier, FOType> validatorGlobals = new HashMap<>();
//        Map<Identifier, StructType> validatorStructs = new HashMap<>();
//        initializeFreshContext(validatorFunctions, validatorGlobals, validatorStructs);
//        for (GlobalVariable global : module.globals) {
//            localVariables = new MapStack<>();
//            expressionValidator = new ExpressionValidator(localVariables, validatorFunctions, validatorGlobals, global.name.removeLast(), validatorStructs, this);
//            currentModPath = global.name.removeLast();
//            global.type = resolveType(global.type);
//            global.value = global.value.request(expressionValidator, global.type);
//            Utils.requireType(global.type, global.value);
//        }
//    }
//
//    private FOType expectedReturnType;
//    private Identifier currentModPath;
//    private MapStack<Identifier, FOType> localVariables;
//    private ExpressionValidator expressionValidator;
//
//    private void visitFunction(Function function) {
//        currentModPath = function.getModPath();
//
//        Map<Identifier, FunctionType> validatorFunctions = new HashMap<>();
//        Map<Identifier, FOType> validatorGlobals = new HashMap<>();
//        Map<Identifier, StructType> validatorStructs = new HashMap<>();
//        initializeFreshContext(validatorFunctions, validatorGlobals, validatorStructs);
//        function.type = resolveFunction(function.type);
//        if (!function.isExtern()) {
//            localVariables = new MapStack<>();
//            expressionValidator = new ExpressionValidator(localVariables, validatorFunctions, validatorGlobals, currentModPath, validatorStructs, this);
//            localVariables.push();
//            for (int i = 0; i < function.paramNames.size(); i++) {
//                localVariables.put(function.paramNames.get(i), function.type.params.get(i));
//            }
//            if (!function.body.terminal) {
//                if (function.type.result != FOType.VOID) {
//                    throw new AnalysisException(function.name.span, "function body must be terminal");
//                } else {
//                    function.implicitVoidReturn = true;
//                }
//            }
//
//            expectedReturnType = function.type.result;
//            visitBlock(function.body);
//        }
//    }
//
//    @Override
//    public void visitForStmt(ForStmt forStmt) {
//        forStmt.init.visit(this);
//        forStmt.condition = forStmt.condition.request(expressionValidator, FOType.BOOL).implicitTo(FOType.BOOL);
//        Utils.requireType(FOType.BOOL, forStmt.condition);
//        visitBlock(forStmt.body);
//        forStmt.update.visit(this);
//    }
//
//    @Override
//    public void visitAssign(Assign assign) {
//        if (assign.operator != BinaryOperator.DISCARD_FIRST)
//            throw new AnalysisException(assign.span, "Only DISCARD_FIRST is supported in assignmments currently");
//        assign.b = assign.b.request(expressionValidator, null);
//        assign.a = assign.a.request(expressionValidator, new PointerType(assign.b.result)).implicitTo(new PointerType(assign.b.result)); // TODO request reference?
//        Utils.requireType(new PointerType(assign.b.result), assign.a);
//    }
//
//    @Override
//    public void visitIfStmt(IfStmt ifStmt) {
//        ifStmt.condition = ifStmt.condition.request(expressionValidator, FOType.BOOL).implicitTo(FOType.BOOL);
//        Utils.requireType(FOType.BOOL, ifStmt.condition);
//        visitBlock(ifStmt.then);
//        if (ifStmt.otherwise != null) {
//            visitBlock(ifStmt.otherwise);
//        }
//    }
//
//    @Override
//    public void visitBlock(Block blockStmt) {
//        localVariables.push();
//        blockStmt.statements.forEach(stmt -> stmt.visit(this));
//        localVariables.pop();
//    }
//
//    @Override
//    public void visitReturnStmt(ReturnStmt returnStmt) {
//        if (returnStmt.value == null) Utils.requireType(returnStmt.span, expectedReturnType, FOType.VOID);
//        else {
//            returnStmt.value = returnStmt.value.request(expressionValidator, expectedReturnType).implicitTo(expectedReturnType);
//            Utils.requireType(expectedReturnType, returnStmt.value);
//        }
//    }
//
//    @Override
//    public void visitDeclareVar(DeclareVar declareVar) {
//        if (declareVar.type == null) {
//            declareVar.value = declareVar.value.request(expressionValidator, null);
//            declareVar.type = declareVar.value.result;
//            localVariables.put(declareVar.name, declareVar.type);
//        } else {
//            declareVar.type = resolveType(declareVar.type);
//            localVariables.put(declareVar.name, declareVar.type);
//            declareVar.value = declareVar.value.request(expressionValidator, declareVar.type).implicitTo(declareVar.type);
//            Utils.requireType(declareVar.type, declareVar.value);
//        }
//    }
//
//    @Override
//    public void visitWhileStmt(WhileStmt whileStmt) {
//        whileStmt.condition = whileStmt.condition.request(expressionValidator, FOType.BOOL).implicitTo(FOType.BOOL);
//        Utils.requireType(FOType.BOOL, whileStmt.condition);
//        visitBlock(whileStmt.body);
//    }
//
//    @Override
//    public void visitCallStmt(CallStmt callStmt) {
//        // this is okay because CallExpr is either validated or failed, never modified at the root
//        callStmt.callExpr = (CallExpr) callStmt.callExpr.request(expressionValidator, null);
//    }
//
//    @Override
//    public Identifier getCurrentModPath() {
//        return currentModPath;
//    }
//
//}
