package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.ConcreteType;
import mackycheese21.ferricoxide.GlobalContext;
import mackycheese21.ferricoxide.Variables;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import static org.bytedeco.llvm.global.LLVM.LLVMBuildAlloca;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildStore;

public class DeclareVar extends Ast {

    private final ConcreteType type;
    private final String name;
    private final Ast value;

    public DeclareVar(ConcreteType type, String name, Ast value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

    @Override
    public ConcreteType getConcreteType(GlobalContext globalContext, Variables variables) {
        if (!value.getConcreteType(globalContext, variables).equals(type))
            throw new RuntimeException("invalid code: todo validator exceptions");
        return ConcreteType.NONE;
    }

    @Override
    public LLVMValueRef generateIR(GlobalContext globalContext, Variables variables, LLVMBuilderRef builder) {
        ConcreteType type = value.getConcreteType(globalContext, variables);
        LLVMValueRef alloca = LLVMBuildAlloca(builder, type.llvmTypeRef(), "declarevar");
        LLVMBuildStore(builder, value.generateIR(globalContext, variables, builder), alloca);
        variables.mapAdd(name, new Variables.Entry(alloca, type));
        return null;
    }

    @Override
    public String toString() {
        return String.format("%s %s = %s", type, name, value);
    }
}
