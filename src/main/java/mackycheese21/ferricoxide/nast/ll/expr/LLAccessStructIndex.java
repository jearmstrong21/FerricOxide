package mackycheese21.ferricoxide.nast.ll.expr;

import mackycheese21.ferricoxide.nast.ll.LLContext;
import mackycheese21.ferricoxide.nast.ll.LLValue;
import org.bytedeco.llvm.global.LLVM;

public class LLAccessStructIndex extends LLExpression {

    public final LLExpression object;
    public final int index;

    public LLAccessStructIndex(LLExpression object, int index) {
        this.object = object;
        this.index = index;
    }

    @Override
    public void compile(LLContext ctx) {
        object.compile(ctx);
        value = new LLValue(object.value.type().fields.get(index), LLVM.LLVMBuildStructGEP2(ctx.builder(), object.value.type().ref, object.value.ref(), index, "gep"));
    }

    @Override
    public String toString() {
        return "%s.%s".formatted(object, index);
    }
}
