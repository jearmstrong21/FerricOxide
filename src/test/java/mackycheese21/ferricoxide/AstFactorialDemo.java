package mackycheese21.ferricoxide;

import mackycheese21.ferricoxide.ast.*;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;

import java.util.List;

import static org.bytedeco.llvm.global.LLVM.*;

// https://github.com/bytedeco/javacpp-presets/tree/master/llvm
public class AstFactorialDemo {

    public static void main(String[] args) {
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();

        LLVMModuleRef module = LLVMModuleCreateWithName("main");
        LLVMBuilderRef builder = LLVMCreateBuilder();
        LLVMTypeRef i32Type = LLVMInt32Type();

        GlobalContext globalContext = new GlobalContext();

        globalContext.mapAdd("factorial", new Function("factorial", ConcreteType.I32, List.of(ConcreteType.I32), module));

        LLVMBasicBlockRef entry = LLVMAppendBasicBlock(globalContext.mapGet("factorial").getValueRef(), "entry");

        LLVMPositionBuilderAtEnd(builder, entry);
        Variables variables = globalContext.mapGet("factorial").enter(builder, List.of("n"));

        new Return(
                new If(
                        new IntEq(
                                new AccessVar("n"),
                                new IntConstant(0)
                        ),
                        new IntConstant(1),
                        new Mul(
                                new AccessVar("n"),
                                new FuncCall("factorial", List.of(
                                        new Add(
                                                new AccessVar("n"),
                                                new IntConstant(-1)
                                        )
                                ))
                        )
                )
        )
                .generateIR(globalContext, variables, builder);

        BytePointer error = new BytePointer();
        if (LLVMVerifyModule(module, LLVMPrintMessageAction, error) != 0) {
            LLVMDisposeMessage(error);
            return;
        }

        LLVMPassManagerRef pm = LLVMCreatePassManager();
        LLVMAddAggressiveInstCombinerPass(pm);
        LLVMAddNewGVNPass(pm);
        LLVMAddCFGSimplificationPass(pm);
        LLVMAddPromoteMemoryToRegisterPass(pm); // new pass from demo
        LLVMRunPassManager(pm, module);
        LLVMDumpModule(module);

        LLVMExecutionEngineRef engine = new LLVMExecutionEngineRef();
        LLVMMCJITCompilerOptions options = new LLVMMCJITCompilerOptions();
        if (LLVMCreateMCJITCompilerForModule(engine, module, options, 3, error) != 0) {
            System.err.println("Failed to create JIT compiler: " + error.getString());
            LLVMDisposeMessage(error);
            return;
        }

        LLVMGenericValueRef argument = LLVMCreateGenericValueOfInt(i32Type, 10, 0);
        LLVMGenericValueRef result = LLVMRunFunction(engine, globalContext.mapGet("factorial").getValueRef(), 1, argument);
        System.err.println();
        System.err.println("; Running factorial(10) with MCJIT...");
        System.err.println("; Result: " + LLVMGenericValueToInt(result, 0));

        LLVMDisposeExecutionEngine(engine);
        LLVMDisposePassManager(pm);
        LLVMDisposeBuilder(builder);
    }

}
