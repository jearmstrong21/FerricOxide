package mackycheese21.ferricoxide;

import mackycheese21.ferricoxide.ast.Ast;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.llvm.LLVM.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.bytedeco.llvm.global.LLVM.*;

public class Module {

    public static final class FunctionDecl {
        public static final class Param {
            public final String name;
            public final ConcreteType type;

            public Param(String name, ConcreteType type) {
                this.name = name;
                this.type = type;
            }

            @Override
            public String toString() {
                return String.format("%s:%s", name, type);
            }
        }

        public final String name;
        public final List<Param> params;
        public final Ast body;
        public final ConcreteType result;

        public FunctionDecl(String name, List<Param> params, Ast body, ConcreteType result) {
            this.name = name;
            this.params = params;
            this.body = body;
            this.result = result;
        }
    }

    public final IdentifierMap<FunctionDecl> functions;
    public GlobalContext globalContext;
    public LLVMModuleRef module;

    @Override
    public String toString() {
        return String.format("Module[%s]", functions);
    }

    public Module() {
        functions = new IdentifierMap<>(null);

        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();

//        LLVMInitializeNativeAsmPrinter();
//        LLVMInitializeNativeAsmParser();
//        LLVMInitializeNativeTarget();

        LLVMInitializeAllTargetInfos();
        LLVMInitializeAllTargets();
        LLVMInitializeAllTargetMCs();
        LLVMInitializeAllAsmParsers();
        LLVMInitializeAllAsmPrinters();

//        LLVMInitializeAllTargetInfos();
//        LLVMTargetRef targetRef = LLVMGetFirstTarget();
//        while(targetRef != null) {
//            String name = LLVMGetTargetName(targetRef).getString();
//            String description = LLVMGetTargetDescription(targetRef).getString();
//            System.err.print(name + ": " + description);
//            if(LLVMTargetHasAsmBackend(targetRef) == 1) System.err.print(" (asm)");
//            if(LLVMTargetHasJIT(targetRef) == 1) System.err.print(" (jit)");
//            if(LLVMTargetHasTargetMachine(targetRef) == 1) System.err.print(" (machine)");
//            System.err.println();
//            targetRef = LLVMGetNextTarget(targetRef);
//        }
//        System.err.println(LLVMGetDefaultTargetTriple().getString() + "\n\n");

    }

    public void codegen(String x86_assembly, String x86_bin, String riscv_assembly) {

        globalContext = new GlobalContext();

        module = LLVMModuleCreateWithName("main");
        LLVMBuilderRef builder = LLVMCreateBuilder();

        // LOAD ALL PROTOTYPES
        for (String name : functions.keys()) {
            FunctionDecl decl = functions.mapGet(name);
            globalContext.mapAdd(name, new Function(decl.name, decl.result, decl.params.stream().map(p -> p.type).collect(Collectors.toList()), module, decl.body == null));
        }

        //
        for (String name : functions.keys()) {
            FunctionDecl decl = functions.mapGet(name);
            if(decl.body != null) {
                Variables variables = globalContext.mapGet(name).enter(builder, decl.params.stream().map(p -> p.name).collect(Collectors.toList()));
                decl.body.generateIR(globalContext, variables, builder);
            }
        }

        BytePointer error = new BytePointer();
        if (LLVMVerifyModule(module, LLVMPrintMessageAction, error) != 0) {
            LLVMDisposeMessage(error);
            System.exit(1);
        }

        LLVMPassManagerRef pm = LLVMCreatePassManager();
        LLVMAddNewGVNPass(pm);
        LLVMAddCFGSimplificationPass(pm);
        LLVMAddPromoteMemoryToRegisterPass(pm); // new pass from demo
        LLVMAddAggressiveInstCombinerPass(pm);
        LLVMRunPassManager(pm, module);
        LLVMDisposePassManager(pm);

        if (LLVMVerifyModule(module, LLVMPrintMessageAction, error) != 0) {
            LLVMDisposeMessage(error);
            System.exit(1);
        }

//        outputRISCV();
        outputX86(x86_assembly, x86_bin);
        outputRISCV(riscv_assembly);

//        LLVMDumpModule(module);
        LLVMDisposeBuilder(builder);
    }

    private void outputX86(String assembly, String bin) {
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
            if (LLVMTargetMachineEmitToFile(targetMachineRef, module, new BytePointer(assembly), LLVMAssemblyFile, error) != 0) {
                System.err.println(error.getString());
                LLVMDisposeErrorMessage(error);
                throw new RuntimeException();
            }
        }
        if(bin != null) {
            if (LLVMTargetMachineEmitToFile(targetMachineRef, module, new BytePointer(bin), LLVMObjectFile, error) != 0) {
                System.err.println(error.getString());
                LLVMDisposeErrorMessage(error);
                throw new RuntimeException();
            }
        }
    }

    private void outputRISCV(String filename) {
        if (filename != null) {
            LLVMTargetRef targetRef = LLVMGetTargetFromName("riscv32");
            // level = LLVMCodeGenOptLevel
            // reloc = LLVMRelocMode
            // codeModel = LLVMCodeModel
            // codegen = LLVMCodeGenFileType
            LLVMTargetMachineRef targetMachineRef = LLVMCreateTargetMachine(
                    targetRef, "triple", "generic-rv32", "+f,+m", LLVMCodeGenLevelDefault, LLVMRelocDefault, LLVMCodeModelDefault);

            BytePointer error = new BytePointer();
            if (LLVMTargetMachineEmitToFile(targetMachineRef, module, new BytePointer(filename), LLVMAssemblyFile, error) != 0) {
                System.err.println(error.getString());
                LLVMDisposeErrorMessage(error);
                throw new RuntimeException();
            }
        }
    }

}
