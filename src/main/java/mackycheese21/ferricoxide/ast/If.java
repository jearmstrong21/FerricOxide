package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.ConcreteType;
import mackycheese21.ferricoxide.GlobalContext;
import mackycheese21.ferricoxide.Utils;
import mackycheese21.ferricoxide.Variables;
import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import static org.bytedeco.llvm.global.LLVM.*;

public class If extends Ast {

    private final Ast cond;
    private final Ast then;
    private final Ast otherwise;

    public If(Ast cond, Ast then, Ast otherwise) {
        this.cond = cond;
        this.then = then;
        this.otherwise = otherwise;
    }

    @Override
    public ConcreteType getConcreteType(GlobalContext globalContext, Variables variables) {
        ConcreteType t = then.getConcreteType(globalContext, variables);
        ConcreteType o = otherwise.getConcreteType(globalContext, variables);
        Utils.assertTrue(t.equals(o));
        return t;
    }

    @Override
    public LLVMValueRef generateIR(GlobalContext globalContext, Variables variables, LLVMBuilderRef builder) {
        LLVMBasicBlockRef ifTrue = LLVMAppendBasicBlock(variables.getCurrentFunction().valueRef, "ifTrue");
        LLVMBasicBlockRef ifFalse = LLVMAppendBasicBlock(variables.getCurrentFunction().valueRef, "ifFalse");
        LLVMBasicBlockRef ifAfter = LLVMAppendBasicBlock(variables.getCurrentFunction().valueRef, "ifAfter");

        ConcreteType type = getConcreteType(globalContext, variables);

        LLVMValueRef alloca = LLVMBuildAlloca(builder, type.llvmTypeRef(), "ifResult");

        LLVMBuildCondBr(builder, cond.generateIR(globalContext, variables, builder), ifTrue, ifFalse);

        LLVMPositionBuilderAtEnd(builder, ifTrue);
        LLVMBuildStore(builder, then.generateIR(globalContext, variables, builder), alloca);
        LLVMBuildBr(builder, ifAfter);

        LLVMPositionBuilderAtEnd(builder, ifFalse);
        LLVMBuildStore(builder, otherwise.generateIR(globalContext, variables, builder), alloca);
        LLVMBuildBr(builder, ifAfter);

        LLVMPositionBuilderAtEnd(builder, ifAfter);
        return LLVMBuildLoad(builder, alloca, "ifResult");
    }

    @Override
    public String toString() {
        return String.format("if( %s ) { %s } else { %s }", cond, then, otherwise);
    }
}
