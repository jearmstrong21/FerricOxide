package mackycheese21.ferricoxide.ast.hl.compile;

import mackycheese21.ferricoxide.MapStack;
import mackycheese21.ferricoxide.Pair;
import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.hl.expr.HLExpression;
import mackycheese21.ferricoxide.ast.hl.mod.FunctionDef;
import mackycheese21.ferricoxide.ast.hl.type.HLFunctionType;
import mackycheese21.ferricoxide.ast.hl.type.HLType;
import mackycheese21.ferricoxide.ast.ll.expr.LLExpression;
import mackycheese21.ferricoxide.ast.ll.mod.LLFunction;
import mackycheese21.ferricoxide.ast.ll.mod.LLModule;
import mackycheese21.ferricoxide.ast.ll.stmt.LLStatement;
import mackycheese21.ferricoxide.ast.ll.type.LLStructType;
import mackycheese21.ferricoxide.ast.ll.type.LLType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HLModuleCompiler {

    public static LLModule compile(HLModule hlModule) {
        LLModule llModule = new LLModule(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
        HLTypeLookup typeLookup = new HLTypeLookup(hlModule.globalDefs, hlModule.functionDefs, llModule, hlModule.structDefs);

        for (Identifier identifier : hlModule.structDefs.keySet()) {
            llModule.structs.put(identifier.toLLVMString(), new LLStructType(identifier.toLLVMString(), new ArrayList<>()));
            typeLookup.structCompiled.put(identifier, llModule.structs.get(identifier.toLLVMString()));
        }

        for (Identifier identifier : hlModule.structDefs.keySet()) {
            LLStructType structType = llModule.structs.get(identifier.toLLVMString());
            for (Pair<String, HLType> field : hlModule.structDefs.get(identifier).fields) {
                structType.fields.add(typeLookup.compile(identifier.removeLast(), field.y()));
            }
        }

        /* * * * PROTOTYPES * * * */
        Map<Identifier, HLType> globalTypes = new HashMap<>();
        Map<Identifier, HLFunctionType> functionTypes = new HashMap<>();

        for (Identifier identifier : hlModule.globalDefs.keySet()) {
            llModule.globalTypes.put(identifier.toLLVMString(), typeLookup.compile(identifier.removeLast(), hlModule.globalDefs.get(identifier).type));
            globalTypes.put(identifier, hlModule.globalDefs.get(identifier).type);
        }

        // TODO impls and static methods
        for (Identifier identifier : hlModule.functionDefs.keySet()) {
            FunctionDef functionDef = hlModule.functionDefs.get(identifier);
            llModule.functionTypes.put(functionDef.getLlvmName(), typeLookup.compileFunction(identifier.removeLast(), hlModule.functionDefs.get(identifier).prototype.functionType()));
            functionTypes.put(identifier, hlModule.functionDefs.get(identifier).prototype.functionType());
        }

        /* * * * IMPL * * * */

        for (Identifier identifier : hlModule.globalDefs.keySet()) {
            HLExpressionCompiler compiler = new HLExpressionCompiler(new ArrayList<>(), new MapStack<>(), identifier.removeLast(), typeLookup);
            HLExpression value = hlModule.globalDefs.get(identifier).value;
            LLExpression llValue = value.visit(compiler);
            llModule.globalValues.put(identifier.toLLVMString(), llValue);
        }

        // TODO use llvmName
        for (Identifier identifier : hlModule.functionDefs.keySet()) {
            Identifier modPath = identifier.removeLast();
            FunctionDef functionDef = hlModule.functionDefs.get(identifier);

            List<LLType> locals = new ArrayList<>(functionDef.prototype.params.stream().map(p -> typeLookup.compile(modPath, p.y())).collect(Collectors.toList()));
            HLLocalVisitor visitor = new HLLocalVisitor(locals, modPath, typeLookup);
            if (functionDef.body != null) functionDef.body.forEach(s -> s.visit(visitor));

            HLStatementCompiler stmt = new HLStatementCompiler(modPath, typeLookup, functionDef);
            List<LLStatement> body = null;
            if (functionDef.body != null) {
                body = new ArrayList<>();
                List<LLStatement> finalBody = body;//what the uck java?
                functionDef.body.forEach(s -> finalBody.addAll(s.visit(stmt)));
            }

            llModule.functionValues.put(identifier.toLLVMString(), new LLFunction(functionDef.getLlvmName(), functionDef.inline, typeLookup.compileFunction(modPath, functionDef.prototype.functionType()), body, locals));
        }

        return llModule;
    }

}
