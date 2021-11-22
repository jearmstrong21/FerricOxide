package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.ConcreteType;
import mackycheese21.ferricoxide.GlobalContext;
import mackycheese21.ferricoxide.Variables;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import static org.bytedeco.llvm.global.LLVM.LLVMBuildStore;

public class AssignVar extends Ast {

    private final String name;
    private final Ast value;

    public AssignVar(String name, Ast value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public ConcreteType getConcreteType(GlobalContext globalContext, Variables variables) {
        return ConcreteType.NONE;
    }

    @Override
    public LLVMValueRef generateIR(GlobalContext globalContext, Variables variables, LLVMBuilderRef builder) {
        LLVMBuildStore(builder, value.generateIR(globalContext, variables, builder), variables.mapGet(name).valueRef);
        return null;
    }

    @Override
    public String toString() {
        return String.format("%s = %s", name, value);
    }
}
