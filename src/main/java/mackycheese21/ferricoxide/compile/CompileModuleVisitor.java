package mackycheese21.ferricoxide.compile;

import mackycheese21.ferricoxide.CompilerException;
import mackycheese21.ferricoxide.ast.IdentifierMap;
import mackycheese21.ferricoxide.ast.module.CompiledModule;
import mackycheese21.ferricoxide.ast.module.FOModule;
import mackycheese21.ferricoxide.ast.module.Function;
import mackycheese21.ferricoxide.ast.module.GlobalVariable;
import mackycheese21.ferricoxide.ast.type.ConcreteType;
import mackycheese21.ferricoxide.ast.type.FunctionType;
import mackycheese21.ferricoxide.ast.type.StructType;
import mackycheese21.ferricoxide.ast.visitor.ModuleVisitor;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.llvm.LLVM.*;

import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.llvm.global.LLVM.*;

public class CompileModuleVisitor implements ModuleVisitor<CompiledModule> {

    @Override
    public CompiledModule visit(FOModule module) {
        LLVMModuleRef moduleRef = LLVMModuleCreateWithName("main");
        LLVMBuilderRef builder = LLVMCreateBuilder();

        Map<String, LLVMValueRef> strings = new HashMap<>();
        IdentifierMap<ConcreteType> globalTypes = new IdentifierMap<>(null);
        IdentifierMap<LLVMValueRef> globalRefs = new IdentifierMap<>(null);
        IdentifierMap<StructType> structs = new IdentifierMap<>(null);
        IdentifierMap<FunctionType> functionTypes = new IdentifierMap<>(null);
        IdentifierMap<LLVMValueRef> functionRefs = new IdentifierMap<>(null);

        // Globals
        for (GlobalVariable global : module.globals) {
            globalTypes.mapAdd(global.name, global.type);
            LLVMValueRef valueRef = LLVMAddGlobal(moduleRef, global.type.typeRef, "GlobalVariable");
            globalRefs.mapAdd(global.name, valueRef);
            LLVMSetInitializer(valueRef, global.value.visit(new CompileExpressionVisitor(
                    builder, null, strings, new IdentifierMap<>(null), new IdentifierMap<>(null),
                    new IdentifierMap<>(null), new IdentifierMap<>(null), new IdentifierMap<>(null),
                    new IdentifierMap<>(null), new IdentifierMap<>(null))));
        }

        // Structs
        for (StructType struct : module.structs) {
            structs.mapAdd(struct.name, struct);
        }

        // Function prototypes
        for (Function function : module.functions) {
            LLVMValueRef valueRef = LLVMAddFunction(moduleRef, function.name, function.type.typeRef);
            LLVMSetFunctionCallConv(valueRef, LLVMCCallConv);
            LLVMSetLinkage(valueRef, LLVMExternalLinkage); // (LLVM)Value(Ref) : (LLVM)Global(Ref)

            functionTypes.mapAdd(function.name, function.type);
            functionRefs.mapAdd(function.name, valueRef);
        }

        // Function bodies
        for (Function function : module.functions) {
            if (function.isExtern()) continue;
            LLVMValueRef valueRef = functionRefs.mapGet(function.name);

            LLVMBasicBlockRef entry = LLVMAppendBasicBlock(valueRef, "entry");
            LLVMPositionBuilderAtEnd(builder, entry);

            CompileStatementVisitor compileStatement = new CompileStatementVisitor(builder, valueRef, globalTypes, globalRefs, strings, structs, functionTypes, functionRefs);
            for (int i = 0; i < function.paramNames.size(); i++) {
                LLVMValueRef alloc = LLVMBuildAlloca(builder, function.type.params.get(i).typeRef, "param" + i);
                LLVMBuildStore(builder, LLVMGetParam(valueRef, i), alloc);

                compileStatement.variableTypes.mapAdd(function.paramNames.get(i), function.type.params.get(i));
                compileStatement.variableRefs.mapAdd(function.paramNames.get(i), alloc);
            }

            function.body.visit(compileStatement);
        }

        BytePointer error = new BytePointer();

        if (LLVMPrintModuleToFile(moduleRef, "BIN/build/main-unopt.ll", error) != 0) {
            String errorStr = error.getString();
            LLVMDisposeMessage(error);
            throw CompilerException.moduleVerifyError(errorStr);
        }

        if (LLVMVerifyModule(moduleRef, LLVMReturnStatusAction, error) != 0) {
            String errorStr = error.getString();
            LLVMDisposeMessage(error);
            throw CompilerException.moduleVerifyError(errorStr);
        }

        LLVMPassManagerRef pm = LLVMCreatePassManager();
        LLVMAddStripSymbolsPass(pm);
        LLVMRunPassManager(pm, moduleRef);
        LLVMDisposePassManager(pm);

        if (LLVMPrintModuleToFile(moduleRef, "BIN/build/main-unopt-unnamed.ll", error) != 0) {
            String errorStr = error.getString();
            LLVMDisposeMessage(error);
            throw CompilerException.moduleVerifyError(errorStr);
        }

        pm = LLVMCreatePassManager();
        LLVMAddNewGVNPass(pm);
        LLVMAddCFGSimplificationPass(pm);
        LLVMAddPromoteMemoryToRegisterPass(pm);
        LLVMAddAggressiveInstCombinerPass(pm);
        LLVMAddStripDeadPrototypesPass(pm);
        LLVMAddStripSymbolsPass(pm);
        LLVMRunPassManager(pm, moduleRef);
        LLVMDisposePassManager(pm);

        if (LLVMPrintModuleToFile(moduleRef, "BIN/build/main-opt.ll", error) != 0) {
            String errorStr = error.getString();
            LLVMDisposeMessage(error);
            throw CompilerException.moduleVerifyError(errorStr);
        }

        error = new BytePointer();
        if (LLVMVerifyModule(moduleRef, LLVMReturnStatusAction, error) != 0) {
            String errorStr = error.getString();
            LLVMDisposeMessage(error);
            throw CompilerException.moduleVerifyError(errorStr);
        }

        return new CompiledModule(moduleRef);
    }

}