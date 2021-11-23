package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.ConcreteType;
import mackycheese21.ferricoxide.Module;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.llvm.LLVM.*;

import java.util.List;

import static org.bytedeco.llvm.global.LLVM.*;

// https://github.com/bytedeco/javacpp-presets/tree/master/llvm
public class AstFactorialDemo {

    public static void main(String[] args) {
        Module module = new Module();
        module.functions.mapAdd("factorialRecursive", new Module.FunctionDecl(
                "factorialRecursive",
                List.of(new Module.FunctionDecl.Param("n", ConcreteType.I32)),
                new Return(
                        new If(
                                ArithBinary.Op.EQ.on(
                                        new AccessVar("n"),
                                        new IntConstant(0)
                                ),
                                new mackycheese21.ferricoxide.ast.IntConstant(1),
                                ArithBinary.Op.MUL.on(
                                        new AccessVar("n"),
                                        new FuncCall("factorialRecursive", List.of(
                                                ArithBinary.Op.SUB.on(
                                                        new AccessVar("n"),
                                                        new IntConstant(1)
                                                )
                                        ))
                                )
                        )
                ),
                ConcreteType.I32
        ));
        module.functions.mapAdd("factorialIterative", new Module.FunctionDecl(
                "factorialIterative",
                List.of(new Module.FunctionDecl.Param("n", ConcreteType.I32)),
                new Block(
                        new DeclareVar(ConcreteType.I32, "result", new IntConstant(1)),
                        new WhileLoop(
                                ArithBinary.Op.GE.on(
                                        new AccessVar("n"),
                                        new IntConstant(1)
                                ),
                                new Block(
                                        new AssignVar("result", ArithBinary.Op.MUL.on(
                                                new AccessVar("result"),
                                                new AccessVar("n")
                                        )),
                                        new AssignVar("n", ArithBinary.Op.SUB.on(
                                                new AccessVar("n"),
                                                new IntConstant(1)
                                        ))
                                )
                        ),
                        new Return(new AccessVar("result"))
                ),
                ConcreteType.I32
        ));
        module.codegen(null, null, null);

        LLVMExecutionEngineRef engine = new LLVMExecutionEngineRef();
        LLVMMCJITCompilerOptions options = new LLVMMCJITCompilerOptions();
        BytePointer error = new BytePointer();
        if (LLVMCreateMCJITCompilerForModule(engine, module.module, options, 3, error) != 0) {
            System.err.println("Failed to create JIT compiler: " + error.getString());
            LLVMDisposeMessage(error);
            return;
        }

        LLVMGenericValueRef argument = LLVMCreateGenericValueOfInt(LLVMIntType(32), 10, 0);
        LLVMGenericValueRef result = LLVMRunFunction(engine, module.globalContext.mapGet("factorialRecursive").valueRef, 1, argument);
        System.err.println();
        System.err.println("; Running factorialRecursive(10) with MCJIT...");
        System.err.println("; Result: " + LLVMGenericValueToInt(result, 0));

        argument = LLVMCreateGenericValueOfInt(LLVMIntType(32), 10, 0);
        result = LLVMRunFunction(engine, module.globalContext.mapGet("factorialIterative").valueRef, 1, argument);
        System.err.println();
        System.err.println("; Running factorialIterative(10) with MCJIT...");
        System.err.println("; Result: " + LLVMGenericValueToInt(result, 0));

        LLVMDisposeExecutionEngine(engine);
    }

}
