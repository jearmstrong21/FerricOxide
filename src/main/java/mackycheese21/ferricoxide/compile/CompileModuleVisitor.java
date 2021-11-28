package mackycheese21.ferricoxide.compile;

import mackycheese21.ferricoxide.ast.ConcreteType;
import mackycheese21.ferricoxide.ast.IdentifierMap;
import mackycheese21.ferricoxide.CompilerException;
import mackycheese21.ferricoxide.ast.module.CompiledModule;
import mackycheese21.ferricoxide.ast.module.FOModule;
import mackycheese21.ferricoxide.ast.module.Function;
import mackycheese21.ferricoxide.ast.visitor.ModuleVisitor;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.llvm.LLVM.*;

import static org.bytedeco.llvm.global.LLVM.*;

public class CompileModuleVisitor implements ModuleVisitor<CompiledModule> {

    @Override
    public CompiledModule visit(FOModule module) {
        LLVMModuleRef moduleRef = LLVMModuleCreateWithName("main");
        LLVMBuilderRef builder = LLVMCreateBuilder();

        IdentifierMap<ConcreteType.Function> functionTypes = new IdentifierMap<>(null);
        IdentifierMap<LLVMValueRef> functionRefs = new IdentifierMap<>(null);

        // Function prototypes
        for (Function function : module.functions) {
            LLVMValueRef valueRef = LLVMAddFunction(moduleRef, function.name, function.type.llvmTypeRef());
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

            CompileStatementVisitor compileStatement = new CompileStatementVisitor(builder, valueRef, functionTypes, functionRefs);
            for (int i = 0; i < function.paramNames.size(); i++) {
                LLVMValueRef alloc = LLVMBuildAlloca(builder, function.type.params.get(i).llvmTypeRef(), "param" + i);
                LLVMBuildStore(builder, LLVMGetParam(valueRef, i), alloc);

                compileStatement.variableTypes.mapAdd(function.paramNames.get(i), function.type.params.get(i));
                compileStatement.variableRefs.mapAdd(function.paramNames.get(i), alloc);
            }

            function.body.visit(compileStatement);
        }

        LLVMPassManagerRef pm = LLVMCreatePassManager();
        LLVMAddNewGVNPass(pm);
        LLVMAddCFGSimplificationPass(pm);
        LLVMAddPromoteMemoryToRegisterPass(pm);
        LLVMAddAggressiveInstCombinerPass(pm);
        LLVMRunPassManager(pm, moduleRef);
        LLVMDisposePassManager(pm);

        BytePointer error = new BytePointer();
        if (LLVMVerifyModule(moduleRef, LLVMReturnStatusAction, error) != 0) {
            String errorStr = error.getString();
            LLVMDisposeMessage(error);
            throw CompilerException.moduleVerifyError(errorStr);
        }

        return new CompiledModule(moduleRef);
    }

}
