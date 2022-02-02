package mackycheese21.ferricoxide.nast.ll.expr;

import mackycheese21.ferricoxide.nast.ll.LLContext;
import mackycheese21.ferricoxide.nast.ll.LLType;
import mackycheese21.ferricoxide.nast.ll.LLValue;
import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;
import org.bytedeco.llvm.global.LLVM;

import java.util.List;
import java.util.stream.Collectors;

public class LLLoop extends LLExpression {

    public final List<LLExpression> exprs;

    public LLLoop(List<LLExpression> exprs) {
        this.exprs = exprs;
    }

    @Override
    public void compile(LLContext ctx) {

        LLVMBasicBlockRef start = LLVM.LLVMAppendBasicBlock(ctx.currentFunction(), "start");
        LLVMBasicBlockRef end = LLVM.LLVMAppendBasicBlock(ctx.currentFunction(), "end");

        LLVM.LLVMBuildBr(ctx.builder(), start);

        LLVM.LLVMPositionBuilderAtEnd(ctx.builder(), start);
        ctx.loopBreakTargets().push(end);
        exprs.forEach(e -> e.compile(ctx));
        ctx.loopBreakTargets().pop();
        LLVM.LLVMBuildBr(ctx.builder(), start);

        LLVM.LLVMPositionBuilderAtEnd(ctx.builder(), end);
        value = new LLValue(LLType.none(), LLVM.LLVMGetUndef(LLVM.LLVMVoidType()));
    }

    @Override
    public String toString() {
        return "loop { %s }".formatted(exprs.stream().map(LLExpression::toString).collect(Collectors.joining(" ")));
    }
}
