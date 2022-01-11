package mackycheese21.ferricoxide.ast.ll.compile;

import mackycheese21.ferricoxide.ast.ll.mod.LLModule;
import mackycheese21.ferricoxide.format.LLFormatter;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.llvm.LLVM.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.bytedeco.llvm.global.LLVM.*;

public class LLModuleWriter {

    private static void verify(LLVMModuleRef moduleRef) {
        BytePointer error = new BytePointer();
        if(LLVMVerifyModule(moduleRef, LLVMReturnStatusAction, error) != 0) {
            throw new RuntimeException(error.getString());
        }
    }

    private static void dump(LLVMModuleRef moduleRef, String filename) {
        BytePointer error = new BytePointer();
        if(LLVMPrintModuleToFile(moduleRef, filename, error) != 0) {
            throw new RuntimeException(error.getString());
        }
    }

    public static void write(LLModule module, LLVMModuleRef moduleRef) throws IOException {
        Files.writeString(Path.of("BIN/build/ll_format.txt"), LLFormatter.formatModule("\t", module));


        BytePointer error = new BytePointer();

        BytePointer defaultTriple = LLVMGetDefaultTargetTriple();
        LLVMTargetRef targetRef = new LLVMTargetRef();
        if(LLVMGetTargetFromTriple(defaultTriple, targetRef, error) != 0) {
            throw new RuntimeException(error.getString());
        }

        LLVMTargetMachineRef targetMachine = LLVMCreateTargetMachine(targetRef, defaultTriple.getString(), "generic", "",
                LLVMCodeGenLevelAggressive, LLVMRelocDefault, LLVMCodeModelDefault);

        dump(moduleRef, "BIN/build/llvm_1_names.txt");
        verify(moduleRef);

        LLVMPassManagerRef pm = LLVMCreatePassManager();
        LLVMAddStripSymbolsPass(pm);
        LLVMRunPassManager(pm, moduleRef);

        dump(moduleRef, "BIN/build/llvm_2_nonames.txt");
        verify(moduleRef);

        pm = LLVMCreatePassManager();
        LLVMAddBasicAliasAnalysisPass(pm);
        LLVMAddAnalysisPasses(targetMachine, pm);

        dump(moduleRef, "BIN/build/llvm_3_nonames_opt.txt");
        verify(moduleRef);

        if(LLVMTargetMachineEmitToFile(targetMachine, moduleRef, new BytePointer("BIN/build/main.x86"), LLVMObjectFile, error) != 0) {
            throw new RuntimeException(error.getString());
        }
    }

}
