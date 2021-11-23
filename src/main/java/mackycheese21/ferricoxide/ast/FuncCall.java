package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.ConcreteType;
import mackycheese21.ferricoxide.GlobalContext;
import mackycheese21.ferricoxide.Variables;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.List;
import java.util.stream.Collectors;

import static org.bytedeco.llvm.global.LLVM.LLVMBuildCall;

public class FuncCall extends Ast {

    private final String func;
    private final List<Ast> args;

    public FuncCall(String func, List<Ast> args) {
        this.func = func;
        this.args = args;
    }

    @Override
    public ConcreteType getConcreteType(GlobalContext globalContext, Variables variables) {
        return globalContext.mapGet(func).result;
    }

    @Override
    public LLVMValueRef generateIR(GlobalContext globalContext, Variables variables, LLVMBuilderRef builder) {
        return LLVMBuildCall(builder, globalContext.mapGet(func).valueRef, // TODO function pointers / function pointer types
                new PointerPointer<>(args.size()).put(args.stream().map(arg -> arg.generateIR(globalContext, variables, builder)).toArray(LLVMValueRef[]::new)),
                args.size(), "FuncCall");
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", func, args.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }
}
