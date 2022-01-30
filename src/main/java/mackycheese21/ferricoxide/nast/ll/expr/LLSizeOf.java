package mackycheese21.ferricoxide.nast.ll.expr;

import mackycheese21.ferricoxide.nast.ll.LLContext;
import mackycheese21.ferricoxide.nast.ll.LLType;
import mackycheese21.ferricoxide.nast.ll.LLValue;
import org.bytedeco.llvm.global.LLVM;

public class LLSizeOf extends LLExpression {

    public final LLType type;

    public LLSizeOf(LLType type) {
        this.type = type;
    }

    @Override
    public void compile(LLContext ctx) {
        value = new LLValue(LLType.i32(), LLVM.LLVMSizeOf(type.ref));
    }

    @Override
    public String toString() {
        return "sizeof(%s)".formatted(type);
    }
}
