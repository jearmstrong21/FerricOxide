package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.ConcreteType;
import mackycheese21.ferricoxide.GlobalContext;
import mackycheese21.ferricoxide.Variables;
import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import static org.bytedeco.llvm.global.LLVM.*;

public class WhileLoop extends Ast {

    private final Ast condition;
    private final Ast body;

    public WhileLoop(Ast condition, Ast body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public ConcreteType getConcreteType(GlobalContext globalContext, Variables variables) {
        return null;
    }

    @Override
    public LLVMValueRef generateIR(GlobalContext globalContext, Variables variables, LLVMBuilderRef builder) {
        LLVMBasicBlockRef header = LLVMAppendBasicBlock(variables.getCurrentFunction().getValueRef(), "whileHead");
        LLVMBasicBlockRef body = LLVMAppendBasicBlock(variables.getCurrentFunction().getValueRef(), "whileBody");
        LLVMBasicBlockRef end = LLVMAppendBasicBlock(variables.getCurrentFunction().getValueRef(), "whileEnd");

        LLVMBuildBr(builder, header);

        LLVMPositionBuilderAtEnd(builder, header);
        LLVMBuildCondBr(builder, condition.generateIR(globalContext, variables, builder), body, end);

        LLVMPositionBuilderAtEnd(builder, body);
        this.body.generateIR(globalContext, variables, builder);
        LLVMBuildBr(builder, header);

        LLVMPositionBuilderAtEnd(builder, end);
        return null;
    }

    @Override
    public String toString() {
        return String.format("while %s { %s }", condition, body);
    }
}
