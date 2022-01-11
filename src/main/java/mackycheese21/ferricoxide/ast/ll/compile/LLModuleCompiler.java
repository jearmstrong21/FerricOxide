package mackycheese21.ferricoxide.ast.ll.compile;

import mackycheese21.ferricoxide.ast.ll.mod.LLFunction;
import mackycheese21.ferricoxide.ast.ll.mod.LLModule;
import mackycheese21.ferricoxide.ast.ll.type.LLStructType;
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

    private static Map<String, LLVMValueRef> initializeGlobals(LLVMModuleRef moduleRef, LLModule module, LLTypeCompiler typeCompiler) {
        Map<String, LLVMValueRef> globals = new HashMap<>();
        for (String name : module.globalTypes.keySet()) {
            LLVMTypeRef typeRef = module.globalTypes.get(name).visit(typeCompiler);
            LLVMValueRef valueRef = LLVMAddGlobal(moduleRef, typeRef, name);
            LLVMSetLinkage(valueRef, LLVMExternalLinkage);
            LLVMSetInitializer(valueRef, LLVMConstNull(typeRef));
            globals.put(name, valueRef);
        }
        return globals;
    }

    private static Map<String, LLVMValueRef> initializeFunctions(LLVMModuleRef moduleRef, LLModule module, LLTypeCompiler typeCompiler) {
        Map<String, LLVMValueRef> functions = new HashMap<>();
        for (LLFunction function : module.functionValues.values()) {
            LLVMValueRef valueRef = LLVMAddFunction(moduleRef, function.name, function.type.visit(typeCompiler));
            LLVMSetLinkage(valueRef, LLVMExternalLinkage);
            functions.put(function.name, valueRef);
        }
        return functions;
    }

    private static void compileGlobals(LLVMModuleRef moduleRef, LLModule module, Map<String, LLVMValueRef> globals, Map<String, LLVMValueRef> functions, LLTypeCompiler typeCompiler) {
        LLVMValueRef currentFunction = LLVMAddFunction(moduleRef, "fo_global_init",
                LLVMFunctionType(LLVMVoidType(), new PointerPointer<>(0).put(new LLVMTypeRef[0]), 0, 0));
        LLVMSetLinkage(currentFunction, LLVMExternalLinkage);
        LLVMBuilderRef builder = LLVMCreateBuilder();
        LLVMBasicBlockRef entryBlock = LLVMAppendBasicBlock(currentFunction, "entry");
        LLVMPositionBuilderAtEnd(builder, entryBlock);

        LLExpressionCompiler expressionCompiler = new LLExpressionCompiler(builder, currentFunction, typeCompiler, globals, new ArrayList<>(), functions);
        for (String name : module.globalTypes.keySet()) {
            LLVMBuildStore(builder, module.globalValues.get(name).visit(expressionCompiler), globals.get(name));
        }

        LLVMBuildRetVoid(builder);
    }

    private static void compileFunctions(LLVMModuleRef moduleRef, LLModule module, Map<String, LLVMValueRef> globals, Map<String, LLVMValueRef> functions, LLTypeCompiler typeCompiler) {
        for (LLFunction function : module.functionValues.values()) {
            LLVMValueRef currentFunction = functions.get(function.name);

            if (function.statements != null) {
                LLVMBuilderRef bob = LLVMCreateBuilder();
                LLVMBasicBlockRef entryBlock = LLVMAppendBasicBlock(currentFunction, "entry");
                LLVMPositionBuilderAtEnd(bob, entryBlock);

                List<LLVMValueRef> locals = new ArrayList<>();
                for (int i = 0; i < function.locals.size(); i++) {
                    locals.add(LLVMBuildAlloca(bob, function.locals.get(i).visit(typeCompiler), "" + i));
                }
                for (int i = 0; i < function.type.params.size(); i++) {
                    LLVMBuildStore(bob, LLVMGetParam(currentFunction, i), locals.get(i));
                }
                LLExpressionCompiler expressionCompiler = new LLExpressionCompiler(bob, currentFunction, typeCompiler, globals, locals, functions);
                LLStatementCompiler statementCompiler = new LLStatementCompiler(bob, currentFunction, expressionCompiler);

                function.statements.forEach(s -> s.visit(statementCompiler));
            }
        }
    }

    // CONTRACT already validated
    public static LLVMModuleRef compile(LLModule module) {
        LLVMModuleRef moduleRef = LLVMModuleCreateWithName("main");

        LLTypeCompiler typeCompiler = compileStructs(module);

        Map<String, LLVMValueRef> globals = initializeGlobals(moduleRef, module, typeCompiler);
        Map<String, LLVMValueRef> functions = initializeFunctions(moduleRef, module, typeCompiler);

        compileGlobals(moduleRef, module, globals, functions, typeCompiler);

        compileFunctions(moduleRef, module, globals, functions, typeCompiler);

        return moduleRef;
    }

}
