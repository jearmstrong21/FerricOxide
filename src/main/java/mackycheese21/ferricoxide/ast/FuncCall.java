package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.ConcreteType;
import mackycheese21.ferricoxide.GlobalContext;
import mackycheese21.ferricoxide.Variables;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.List;

import static org.bytedeco.llvm.global.LLVM.*;

public class FuncCall extends Ast {

    private final Ast func;
    private final List<Ast> args;

    public FuncCall(Ast func, List<Ast> args) {
        super(ConcreteType.I32);
        this.func = func;
        this.args = args;
    }

    @Override
    public LLVMValueRef generateIR(GlobalContext globalContext, Variables variables, LLVMBuilderRef builder) {
        return LLVMBuildCall(builder, func.generateIR(globalContext, variables, builder),
                new PointerPointer<>(args.size()).put(args.stream().map(arg -> arg.generateIR(globalContext, variables, builder)).toArray(LLVMValueRef[]::new)),
                args.size(), "FuncCall");
    }
}
