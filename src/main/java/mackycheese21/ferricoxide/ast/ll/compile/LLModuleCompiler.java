package mackycheese21.ferricoxide.ast.ll.compile;

import mackycheese21.ferricoxide.Pair;
import mackycheese21.ferricoxide.ast.ll.mod.LLFunction;
import mackycheese21.ferricoxide.ast.ll.mod.LLModule;
import mackycheese21.ferricoxide.ast.ll.type.LLFunctionType;
import mackycheese21.ferricoxide.ast.ll.type.LLPointerType;
import mackycheese21.ferricoxide.ast.ll.type.LLStructType;
import mackycheese21.ferricoxide.ast.ll.type.LLType;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bytedeco.llvm.global.LLVM.*;

public class LLModuleCompiler {

    private static LLTypeCompiler compileStructs(LLModule module) {
        Map<LLStructType, LLVMTypeRef> structs = new HashMap<>();
        for (LLStructType struct : module.structs.values()) {
            LLVMTypeRef typeRef = LLVMStructCreateNamed(LLVMGetGlobalContext(), struct.name);
            structs.put(struct, typeRef);
        }
        LLTypeCompiler typeCompiler = new LLTypeCompiler(structs);
        for (LLStructType struct : module.structs.values()) {
            LLVMTypeRef[] fields = new LLVMTypeRef[struct.fields.size()];
            for (int i = 0; i < fields.length; i++) {
                fields[i] = struct.fields.get(i).visit(typeCompiler);
            }
            //last param = packed
            LLVMStructSetBody(structs.get(struct), new PointerPointer<>(fields.length).put(fields), fields.length, 0);
        }
        return typeCompiler;
    }

    private static Pair<Map<String, LLType>, Map<String, LLVMValueRef>> initializeGlobals(LLVMModuleRef moduleRef, LLModule module, LLTypeCompiler typeCompiler) {
        Map<String, LLType> globalTypes = new HashMap<>();
        Map<String, LLVMValueRef> globalRefs = new HashMap<>();
        for (String name : module.globalTypes.keySet()) {
            LLVMTypeRef typeRef = module.globalTypes.get(name).visit(typeCompiler);
            LLVMValueRef valueRef = LLVMAddGlobal(moduleRef, typeRef, name);
            LLVMSetLinkage(valueRef, LLVMExternalLinkage);
            LLVMSetInitializer(valueRef, LLVMConstNull(typeRef));
            globalTypes.put(name, new LLPointerType(module.globalTypes.get(name)));
            globalRefs.put(name, valueRef);
        }
        return new Pair<>(globalTypes, globalRefs);
    }

    private static Pair<Map<String, LLFunctionType>, Map<String, LLVMValueRef>> initializeFunctions(LLVMModuleRef moduleRef, LLModule module, LLTypeCompiler typeCompiler) {
        Map<String, LLFunctionType> functionTypes = new HashMap<>();
        Map<String, LLVMValueRef> functionRefs = new HashMap<>();
        for (LLFunction function : module.functionValues.values()) {
            LLVMValueRef valueRef = LLVMAddFunction(moduleRef, function.name, function.type.visit(typeCompiler));
            LLVMSetLinkage(valueRef, LLVMExternalLinkage);
            functionTypes.put(function.name, function.type);
            functionRefs.put(function.name, valueRef);
        }
        return new Pair<>(functionTypes, functionRefs);
    }

    private static void compileGlobals(LLVMModuleRef moduleRef, LLModule module, Map<String, LLType> globalTypes, Map<String, LLVMValueRef> globalRefs, Map<String, LLFunctionType> functionTypes, Map<String, LLVMValueRef> functionRefs, LLTypeCompiler typeCompiler) {
        LLVMValueRef currentFunction = LLVMAddFunction(moduleRef, "fo_global_init",
                LLVMFunctionType(LLVMVoidType(), new PointerPointer<>(0).put(new LLVMTypeRef[0]), 0, 0));
        LLVMSetLinkage(currentFunction, LLVMExternalLinkage);
        LLVMBuilderRef builder = LLVMCreateBuilder();
        LLVMBasicBlockRef entryBlock = LLVMAppendBasicBlock(currentFunction, "entry");
        LLVMPositionBuilderAtEnd(builder, entryBlock);

        LLExpressionCompiler expressionCompiler = new LLExpressionCompiler(builder, currentFunction, typeCompiler, globalTypes, globalRefs, new ArrayList<>(), new ArrayList<>(), functionTypes, functionRefs);
        for (String name : module.globalTypes.keySet()) {
            LLVMBuildStore(builder, module.globalValues.get(name).visit(expressionCompiler), globalRefs.get(name));
        }

        LLVMBuildRetVoid(builder);
    }

    private static void compileFunctions(LLVMModuleRef moduleRef, LLModule module, Map<String, LLType> globalTypes, Map<String, LLVMValueRef> globalRefs, Map<String, LLFunctionType> functionTypes, Map<String, LLVMValueRef> functionRefs, LLTypeCompiler typeCompiler) {
        for (LLFunction function : module.functionValues.values()) {
            LLVMValueRef currentFunction = functionRefs.get(function.name);

            if (function.statements != null) {
                LLVMBuilderRef bob = LLVMCreateBuilder(); // HE CAN DO IT
                LLVMBasicBlockRef entryBlock = LLVMAppendBasicBlock(currentFunction, "entry");
                LLVMPositionBuilderAtEnd(bob, entryBlock);

                List<LLType> localTypes = new ArrayList<>();
                List<LLVMValueRef> localRefs = new ArrayList<>();
                for (int i = 0; i < function.locals.size(); i++) {
                    localTypes.add(new LLPointerType(function.locals.get(i)));
                    localRefs.add(LLVMBuildAlloca(bob, function.locals.get(i).visit(typeCompiler), "" + i));
                }
                for (int i = 0; i < function.locals.size(); i++) {
                    LLVMBuildStore(bob, LLVMGetParam(currentFunction, i), localRefs.get(i));
                }
                LLExpressionCompiler expressionCompiler = new LLExpressionCompiler(bob, currentFunction, typeCompiler, globalTypes, globalRefs, localTypes, localRefs, functionTypes, functionRefs);
                LLStatementCompiler statementCompiler = new LLStatementCompiler(bob, currentFunction, expressionCompiler);

                function.statements.forEach(s -> s.visit(statementCompiler));
            }
        }
    }

    // CONTRACT already validated
    public static LLVMModuleRef compile(LLModule module) {
        LLVMModuleRef moduleRef = LLVMModuleCreateWithName("main");

        LLTypeCompiler typeCompiler = compileStructs(module);

        Pair<Map<String, LLType>, Map<String, LLVMValueRef>> globals = initializeGlobals(moduleRef, module, typeCompiler);
        Map<String, LLType> globalTypes = globals.x();
        Map<String, LLVMValueRef> globalRefs = globals.y();
        Pair<Map<String, LLFunctionType>, Map<String, LLVMValueRef>> functions = initializeFunctions(moduleRef, module, typeCompiler);
        Map<String, LLFunctionType> functionTypes = functions.x();
        Map<String, LLVMValueRef> functionRefs = functions.y();

        compileGlobals(moduleRef, module, globalTypes, globalRefs, functionTypes, functionRefs, typeCompiler);

        compileFunctions(moduleRef, module, globalTypes, globalRefs, functionTypes, functionRefs, typeCompiler);

        return moduleRef;
    }

}
