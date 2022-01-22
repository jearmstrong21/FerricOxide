package mackycheese21.ferricoxide.nast.ll;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.llvm.LLVM.*;
import org.bytedeco.llvm.global.LLVM;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LLModule {

    public final String name;
    public final Map<String, LLType> globals;
    public final Map<String, LLFunction> functions;
    public final Map<String, LLType> structs;

    public LLVMModuleRef ref;

    public LLModule(String name) {
        this.name = name;
        globals = new HashMap<>();
        functions = new HashMap<>();
        structs = new HashMap<>();
    }

    public void compile() {
        ref = LLVM.LLVMModuleCreateWithName(name);

        Map<String, LLValue> globalValues = new HashMap<>();
        Map<String, LLValue> functionValues = new HashMap<>();

        for (String name : globals.keySet()) {
            LLType global = globals.get(name);
            LLVMValueRef globalRef = LLVM.LLVMAddGlobal(ref, global.ref, name);
            globalValues.put(name, new LLValue(global, globalRef));
        }

        for (String name : functions.keySet()) {
            LLFunction function = functions.get(name);

            LLType type = LLType.function(function.locals().subList(0, function.paramCount()), function.result());
            LLVMValueRef functionRef = LLVM.LLVMAddFunction(ref, name, type.ref);

            LLVM.LLVMSetLinkage(functionRef, function.externLinkage() ? LLVM.LLVMExternalLinkage : LLVM.LLVMPrivateLinkage);

            functionValues.put(name, new LLValue(type, functionRef));
        }

        for (String name : functions.keySet()) {
            LLFunction function = functions.get(name);
            if (function.body() != null) {
                LLVMValueRef functionRef = functionValues.get(name).ref();

                List<LLValue> locals = new ArrayList<>();
                LLContext ctx = new LLContext(LLVM.LLVMCreateBuilder(), locals, globalValues, functionValues, structs);

                LLVMBasicBlockRef entry = LLVM.LLVMAppendBasicBlock(functionRef, "entry");
                LLVM.LLVMPositionBuilderAtEnd(ctx.builder(), entry);

                for (int i = 0; i < function.locals().size(); i++) {
                    LLType type = function.locals().get(i);
                    locals.add(new LLValue(LLType.pointer(type), LLVM.LLVMBuildAlloca(ctx.builder(), type.ref, "%" + i)));
                }

                for (int i = 0; i < function.paramCount(); i++) {
                    LLVM.LLVMBuildStore(ctx.builder(), LLVM.LLVMGetParam(functionRef, i), locals.get(i).ref());
                }

                function.body().compile(ctx);
            }
        }

    }

    private void verify() {
        BytePointer error = new BytePointer();
        if (LLVM.LLVMVerifyModule(ref, LLVM.LLVMReturnStatusAction, error) != 0) {
            throw new RuntimeException(error.getString());
        }
    }

    private void dump(String filename) {
        BytePointer error = new BytePointer();
        if (LLVM.LLVMPrintModuleToFile(ref, filename, error) != 0) {
            throw new RuntimeException(error.getString());
        }
    }

    public void write(String outputBin) throws IOException {
        Files.writeString(Path.of("BIN/build/ll_format.txt"), toString());


        BytePointer error = new BytePointer();

        BytePointer defaultTriple = LLVM.LLVMGetDefaultTargetTriple();
        LLVMTargetRef targetRef = new LLVMTargetRef();
        if (LLVM.LLVMGetTargetFromTriple(defaultTriple, targetRef, error) != 0) {
            throw new RuntimeException(error.getString());
        }

        LLVMTargetMachineRef targetMachine = LLVM.LLVMCreateTargetMachine(targetRef, defaultTriple.getString(), "generic", "",
                LLVM.LLVMCodeGenLevelAggressive, LLVM.LLVMRelocDefault, LLVM.LLVMCodeModelDefault);

        dump("BIN/build/llvm_1_names.txt");
        verify();

        LLVMPassManagerRef pm = LLVM.LLVMCreatePassManager();
        LLVM.LLVMAddStripSymbolsPass(pm);
        LLVM.LLVMRunPassManager(pm, ref);

        dump("BIN/build/llvm_2_nonames.txt");
        verify();

        pm = LLVM.LLVMCreatePassManager();
        LLVM.LLVMAddBasicAliasAnalysisPass(pm);
        LLVM.LLVMAddAnalysisPasses(targetMachine, pm);

        dump("BIN/build/llvm_3_nonames_opt.txt");
        verify();

        if (LLVM.LLVMTargetMachineEmitToFile(targetMachine, ref, new BytePointer(outputBin), LLVM.LLVMObjectFile, error) != 0) {
            throw new RuntimeException(error.getString());
        }
    }

}
