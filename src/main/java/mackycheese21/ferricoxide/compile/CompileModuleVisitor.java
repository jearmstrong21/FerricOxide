//package mackycheese21.ferricoxide.compile;
//
//import mackycheese21.ferricoxide.MapStack;
//import mackycheese21.ferricoxide.Identifier;
//import mackycheese21.ferricoxide.ast.module.CompiledModule;
//import mackycheese21.ferricoxide.ast.module.FOModule;
//import mackycheese21.ferricoxide.ast.module.Function;
//import mackycheese21.ferricoxide.ast.module.GlobalVariable;
//import mackycheese21.ferricoxide.ast.type.FOType;
//import mackycheese21.ferricoxide.ast.type.FunctionType;
//import mackycheese21.ferricoxide.ast.type.StructType;
//import mackycheese21.ferricoxide.ast.type.TypeRegistry;
//import mackycheese21.ferricoxide.ast.visitor.ModuleVisitor;
//import org.bytedeco.javacpp.BytePointer;
//import org.bytedeco.llvm.LLVM.*;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.bytedeco.llvm.global.LLVM.*;
//
//public class CompileModuleVisitor implements ModuleVisitor<CompiledModule> {
//
//    @Override
//    public CompiledModule visit(FOModule module) {
//        LLVMModuleRef moduleRef = LLVMModuleCreateWithName("main");
//        LLVMBuilderRef builder = LLVMCreateBuilder();
//
//        Map<Identifier, FOType> globalTypes = new HashMap<>();
//        Map<Identifier, LLVMValueRef> globalRefs = new HashMap<>();
//        Map<Identifier, LLVMValueRef> functionRefs = new HashMap<>();
//        Map<Identifier, StructType> structs = new HashMap<>();
//
//        // Structs
//        for (StructType struct : module.structs) {
//            structs.put(struct.identifier, struct);
//        }
//
//        // Function prototypes
//        for (Function function : module.functions) {
//            LLVMValueRef valueRef = LLVMAddFunction(moduleRef, function.llvmName == null ? function.name.toLLVMString() : function.llvmName, TypeRegistry.forceLookup(function.type));
//            LLVMSetFunctionCallConv(valueRef, LLVMCCallConv);
//            LLVMSetLinkage(valueRef, LLVMExternalLinkage); // (LLVM)Value(Ref) : (LLVM)Global(Ref)
//
//            functionRefs.put(function.name, valueRef);
//        }
//
//        // Globals
//        for (GlobalVariable global : module.globals) {
//            LLVMTypeRef typeRef = TypeRegistry.forceLookup(global.type);
//            globalTypes.put(global.name, global.type);
//            LLVMValueRef valueRef = LLVMAddGlobal(moduleRef, typeRef, global.name.toString());
//
//            LLVMSetInitializer(valueRef, LLVMConstNull(typeRef));
//
//            globalRefs.put(global.name, valueRef);
//        }
//
//        {
//            FunctionType functionType = new FunctionType(FOType.VOID, new ArrayList<>());
//            LLVMValueRef currentFunction = LLVMAddFunction(moduleRef, "fo_global_init", TypeRegistry.forceLookup(functionType));
//            LLVMSetFunctionCallConv(currentFunction, LLVMCCallConv);
//            LLVMSetLinkage(currentFunction, LLVMExternalLinkage);
//            LLVMBasicBlockRef entry = LLVMAppendBasicBlock(currentFunction, "fo_global_init");
//            LLVMPositionBuilderAtEnd(builder, entry);
//            for (GlobalVariable global : module.globals) {
//                LLVMBuildStore(builder, global.value.visit(new CompileExpressionVisitor(
//                        builder,
//                        currentFunction,
//                        globalTypes,
//                        globalRefs,
//                        functionRefs,
//                        new MapStack<>(), /* localTypes */
//                        new MapStack<>()  /* localRefs */
//                )), globalRefs.get(global.name));
//            }
//            LLVMBuildRetVoid(builder);
//        }
//
//        // Function bodies
//        for (Function function : module.functions) {
//            if (function.isExtern()) continue;
//            LLVMValueRef valueRef = functionRefs.get(function.name);
//
//            LLVMBasicBlockRef entry = LLVMAppendBasicBlock(valueRef, function.name.toString());
//            LLVMPositionBuilderAtEnd(builder, entry);
//
//            AllDeclaredVariablesVisitor allDeclaredVariablesVisitor = new AllDeclaredVariablesVisitor(builder);
//            allDeclaredVariablesVisitor.visitBlock(function.body);
//
//            CompileStatementVisitor compileStatement = new CompileStatementVisitor(
//                    builder,
//                    valueRef,
//                    allDeclaredVariablesVisitor.variableRefs,
//                    globalTypes,
//                    globalRefs,
//                    functionRefs
//            );
//            compileStatement.variableTypes.push();
//            compileStatement.localVariableRefs.push();
//            for (int i = 0; i < function.paramNames.size(); i++) {
//                LLVMValueRef alloc = LLVMBuildAlloca(builder, TypeRegistry.forceLookup(function.type.params.get(i)), "param" + i);
//                LLVMBuildStore(builder, LLVMGetParam(valueRef, i), alloc);
//
//                compileStatement.variableTypes.put(function.paramNames.get(i), function.type.params.get(i));
//                compileStatement.localVariableRefs.put(function.paramNames.get(i), alloc);
//            }
//
//            function.body.visit(compileStatement);
//
//            if (function.implicitVoidReturn) LLVMBuildRetVoid(builder);
//        }
//
//        BytePointer error = new BytePointer();
//
//        if (LLVMPrintModuleToFile(moduleRef, "BIN/build/main-unopt.ll", error) != 0) {
//            String errorStr = error.getString();
//            LLVMDisposeMessage(error);
//            throw new AssertionError(errorStr);
//        }
//
//        if (LLVMVerifyModule(moduleRef, LLVMReturnStatusAction, error) != 0) {
//            String errorStr = error.getString();
//            LLVMDisposeMessage(error);
//            throw new AssertionError(errorStr);
//        }
//
//        LLVMPassManagerRef pm = LLVMCreatePassManager();
//        LLVMAddStripSymbolsPass(pm);
//        LLVMRunPassManager(pm, moduleRef);
//        LLVMDisposePassManager(pm);
//
//        if (LLVMPrintModuleToFile(moduleRef, "BIN/build/main-unopt-unnamed.ll", error) != 0) {
//            String errorStr = error.getString();
//            LLVMDisposeMessage(error);
//            throw new AssertionError(errorStr);
//        }
//
//        pm = LLVMCreatePassManager();
//        LLVMAddAggressiveInstCombinerPass(pm);
//        LLVMAddPromoteMemoryToRegisterPass(pm);
//        LLVMAddStripDeadPrototypesPass(pm);
//        LLVMAddStripSymbolsPass(pm);
//        LLVMRunPassManager(pm, moduleRef);
//        LLVMDisposePassManager(pm);
//
//        if (LLVMPrintModuleToFile(moduleRef, "BIN/build/main-opt.ll", error) != 0) {
//            String errorStr = error.getString();
//            LLVMDisposeMessage(error);
//            throw new AssertionError(errorStr);
//        }
//
//        error = new BytePointer();
//        if (LLVMVerifyModule(moduleRef, LLVMReturnStatusAction, error) != 0) {
//            String errorStr = error.getString();
//            LLVMDisposeMessage(error);
//            throw new AssertionError(errorStr);
//        }
//
//        return new CompiledModule(moduleRef);
//    }
//
//}
