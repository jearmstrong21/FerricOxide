package mackycheese21.ferricoxide.nast.ll.expr;

import mackycheese21.ferricoxide.nast.ll.LLContext;
import mackycheese21.ferricoxide.nast.ll.LLValue;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;
import org.bytedeco.llvm.global.LLVM;

public class LLIfExpr extends LLExpression {

    public final LLExpression condition, then, otherwise;

    public LLIfExpr(LLExpression condition, LLExpression then, LLExpression otherwise) {
        this.condition = condition;
        this.then = then;
        this.otherwise = otherwise;
    }

    @Override
    public void compile(LLContext ctx) {
        condition.compile(ctx);
        LLVMBasicBlockRef thenBlock = LLVM.LLVMAppendBasicBlock(ctx.currentFunction(), "then");
        LLVMBasicBlockRef otherwiseBlock = LLVM.LLVMAppendBasicBlock(ctx.currentFunction(), "otherwise");
        LLVMBasicBlockRef end = LLVM.LLVMAppendBasicBlock(ctx.currentFunction(), "end");

        LLVM.LLVMBuildCondBr(ctx.builder(), condition.value.ref(), thenBlock, otherwiseBlock);

        LLVM.LLVMPositionBuilderAtEnd(ctx.builder(), thenBlock);
        then.compile(ctx);
        LLVM.LLVMBuildBr(ctx.builder(), end);

        LLVM.LLVMPositionBuilderAtEnd(ctx.builder(), otherwiseBlock);
        otherwise.compile(ctx);
        LLVM.LLVMBuildBr(ctx.builder(), end);

        LLVM.LLVMPositionBuilderAtEnd(ctx.builder(), end);
        LLVMValueRef phi = LLVM.LLVMBuildPhi(ctx.builder(), then.value.type().ref, "phi");
        LLVM.LLVMAddIncoming(phi, new PointerPointer<>(2).put(new LLVMValueRef[]{
                then.value.ref(),
                otherwise.value.ref()
        }), new PointerPointer<>(2).put(new LLVMBasicBlockRef[]{
                thenBlock,
                otherwiseBlock
        }), 2);

        value = new LLValue(then.value.type(), phi);
    }

    @Override
    public String toString() {
        return "if %s { %s } else { %s }".formatted(condition, then, otherwise);
    }
}
