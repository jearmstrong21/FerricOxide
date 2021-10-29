package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.ConcreteType;
import mackycheese21.ferricoxide.GlobalContext;
import mackycheese21.ferricoxide.Variables;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

public class AccessVar extends Ast {

    private final String name;

    public AccessVar(String name) {
        super(ConcreteType.I32);
        this.name = name;
    }

    @Override
    public LLVMValueRef generateIR(GlobalContext globalContext, Variables variables, LLVMBuilderRef builder) {
        if(variables.mapHas(name)) {
            return variables.mapGet(name);
        } else {
            return globalContext.mapGet(name).getValueRef();
        }
    }
}
