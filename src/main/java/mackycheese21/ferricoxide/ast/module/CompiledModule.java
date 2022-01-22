package mackycheese21.ferricoxide.ast.module;

import mackycheese21.ferricoxide.ast.ll.mod.LLModule;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.llvm.LLVM.LLVMModuleRef;
import org.bytedeco.llvm.LLVM.LLVMTargetMachineRef;
import org.bytedeco.llvm.LLVM.LLVMTargetRef;

import static org.bytedeco.llvm.global.LLVM.*;

public class CompiledModule {

    private final LLModule llModule;
    private final LLVMModuleRef moduleRef;

    public CompiledModule(LLModule llModule, LLVMModuleRef moduleRef) {
        this.llModule = llModule;
        this.moduleRef = moduleRef;
    }

    public void outputX86(String assembly, String bin) {
        BytePointer error = new BytePointer();

        BytePointer defaultTriple = LLVMGetDefaultTargetTriple();
        LLVMTargetRef targetRef = new LLVMTargetRef();
        if (LLVMGetTargetFromTriple(defaultTriple, targetRef, error) != 0) {
            System.err.println(error.getString());
            LLVMDisposeErrorMessage(error);
            throw new RuntimeException();
        }
        LLVMTargetMachineRef targetMachineRef = LLVMCreateTargetMachine(targetRef, defaultTriple,
                new BytePointer("generic"), new BytePointer(""),
                LLVMCodeGenLevelDefault, LLVMRelocDefault, LLVMCodeModelDefault);
//        LLVMSetDataLayout(module, LLVMCreateTargetDataLayout();
//        LLVMSetTarget(module, defaultTriple);
        if (assembly != null) {
            if (LLVMTargetMachineEmitToFile(targetMachineRef, moduleRef, new BytePointer(assembly), LLVMAssemblyFile, error) != 0) {
                System.err.println(error.getString());
                LLVMDisposeErrorMessage(error);
                throw new RuntimeException();
            }
        }
        if (bin != null) {
            if (LLVMTargetMachineEmitToFile(targetMachineRef, moduleRef, new BytePointer(bin), LLVMObjectFile, error) != 0) {
                System.err.println(error.getString());
                LLVMDisposeErrorMessage(error);
                throw new RuntimeException();
            }
        }
    }

    public void outputRISCV(String filename) {
        LLVMTargetRef targetRef = LLVMGetTargetFromName("riscv32");
        // level = LLVMCodeGenOptLevel
        // reloc = LLVMRelocMode
        // codeModel = LLVMCodeModel
        // codegen = LLVMCodeGenFileType
        LLVMTargetMachineRef targetMachineRef = LLVMCreateTargetMachine(
                targetRef, "triple", "generic-rv32", "+f,+m", LLVMCodeGenLevelDefault, LLVMRelocDefault, LLVMCodeModelDefault);

        BytePointer error = new BytePointer();
        if (LLVMTargetMachineEmitToFile(targetMachineRef, moduleRef, new BytePointer(filename), LLVMAssemblyFile, error) != 0) {
            System.err.println(error.getString());
            LLVMDisposeErrorMessage(error);
            throw new RuntimeException();
        }
    }

}
