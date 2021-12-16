package mackycheese21.ferricoxide.compile;

import mackycheese21.ferricoxide.CompilerException;
import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.IdentifierMap;
import mackycheese21.ferricoxide.ast.module.CompiledModule;
import mackycheese21.ferricoxide.ast.module.FOModule;
import mackycheese21.ferricoxide.ast.module.Function;
import mackycheese21.ferricoxide.ast.module.GlobalVariable;
import mackycheese21.ferricoxide.ast.type.ConcreteType;
import mackycheese21.ferricoxide.ast.type.PointerType;
import mackycheese21.ferricoxide.ast.type.StructType;
import mackycheese21.ferricoxide.ast.visitor.AllDeclaredVariablesVisitor;
import mackycheese21.ferricoxide.ast.visitor.ModuleVisitor;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.llvm.LLVM.*;

import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.llvm.global.LLVM.*;

public class CompileModuleVisitor implements ModuleVisitor<CompiledModule> {

    @Override
    public CompiledModule visit(FOModule module) {
        module.initializeGlobals();
        LLVMModuleRef moduleRef = LLVMModuleCreateWithName("main");
        LLVMBuilderRef builder = LLVMCreateBuilder();

        Map<String, LLVMValueRef> strings = new HashMap<>();
        IdentifierMap<ConcreteType> globalTypes = new IdentifierMap<>(null);
        IdentifierMap<LLVMValueRef> globalRefs = new IdentifierMap<>(null);
        IdentifierMap<StructType> structs = new IdentifierMap<>(null);

        // Structs
        for (StructType struct : module.structs) {
            structs.mapAdd(struct.identifier, struct);
        }

        // Function prototypes
        for (Function function : module.functions) {
            LLVMValueRef valueRef = LLVMAddFunction(moduleRef, function.llvmName == null ? function.name.toLLVMString() : function.llvmName, function.type.typeRef);
            LLVMSetFunctionCallConv(valueRef, LLVMCCallConv);
            LLVMSetLinkage(valueRef, LLVMExternalLinkage); // (LLVM)Value(Ref) : (LLVM)Global(Ref)

            globalTypes.mapAdd(function.name, function.type);
            globalRefs.mapAdd(function.name, valueRef);
        }

        // Globals
        for (GlobalVariable global : module.globals) {
            globalTypes.mapAdd(global.name, global.type);
            LLVMValueRef valueRef = LLVMAddGlobal(moduleRef, global.type.typeRef, global.name.toString());

            LLVMSetInitializer(valueRef, LLVMConstNull(global.type.typeRef));

            globalRefs.mapAdd(global.name, valueRef);
        }

        // Function bodies
        for (Function function : module.functions) {
            if (function.isExtern()) continue;
            LLVMValueRef valueRef = globalRefs.mapGet(function.name);

            LLVMBasicBlockRef entry = LLVMAppendBasicBlock(valueRef, function.name.toString());
            LLVMPositionBuilderAtEnd(builder, entry);

            AllDeclaredVariablesVisitor allDeclaredVariablesVisitor = new AllDeclaredVariablesVisitor(builder);
            allDeclaredVariablesVisitor.visitBlock(function.body);

            CompileStatementVisitor compileStatement = new CompileStatementVisitor(builder,
                    valueRef,
                    allDeclaredVariablesVisitor.variableRefs,
                    globalTypes,
                    globalRefs,
                    strings,
                    structs);
            for (int i = 0; i < function.paramNames.size(); i++) {
                LLVMValueRef alloc = LLVMBuildAlloca(builder, function.type.params.get(i).typeRef, "param" + i);
                LLVMBuildStore(builder, LLVMGetParam(valueRef, i), alloc);

                compileStatement.variableTypes.mapAdd(new Identifier(function.paramNames.get(i), false), function.type.params.get(i));
                compileStatement.localVariableRefs.mapAdd(new Identifier(function.paramNames.get(i), false), alloc);
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
        LLVMAddAggressiveInstCombinerPass(pm);
        LLVMAddPromoteMemoryToRegisterPass(pm);
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
