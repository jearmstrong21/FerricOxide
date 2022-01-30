package mackycheese21.ferricoxide.nast.ll.expr;

import mackycheese21.ferricoxide.nast.ll.LLContext;
import mackycheese21.ferricoxide.nast.ll.LLType;
import mackycheese21.ferricoxide.nast.ll.LLValue;
import org.bytedeco.llvm.global.LLVM;

public class LLFloatConstant extends LLExpression {

    public final double constant;

    public LLFloatConstant(double constant) {
        this.constant = constant;
    }

    @Override
    public void compile(LLContext ctx) {
        value = new LLValue(LLType.f32(), LLVM.LLVMConstReal(LLVM.LLVMFloatType(), constant));
    }

    @Override
    public String toString() {
        return "" + constant;
    }
}
