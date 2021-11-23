package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.ConcreteType;
import mackycheese21.ferricoxide.GlobalContext;
import mackycheese21.ferricoxide.Variables;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import static org.bytedeco.llvm.global.LLVM.LLVMBuildLoad;

public class AccessVar extends Ast {

    private final String name;

    public AccessVar(String name) {
        this.name = name;
    }

    @Override
    public ConcreteType getConcreteType(GlobalContext globalContext, Variables variables) {
        return variables.mapGet(name).type;
    }

    @Override
    public LLVMValueRef generateIR(GlobalContext globalContext, Variables variables, LLVMBuilderRef builder) {
        if (variables.mapHas(name)) {
            return LLVMBuildLoad(builder, variables.mapGet(name).valueRef, "accessvar");
        } else {
            return globalContext.mapGet(name).valueRef;
        }
    }

    @Override
    public String toString() {
        return String.format("access[%s]", name);
    }
}
