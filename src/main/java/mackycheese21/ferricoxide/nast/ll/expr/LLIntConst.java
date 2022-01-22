package mackycheese21.ferricoxide.nast.ll.expr;

import mackycheese21.ferricoxide.nast.ll.LLContext;
import mackycheese21.ferricoxide.nast.ll.LLExpression;
import mackycheese21.ferricoxide.nast.ll.LLType;
import mackycheese21.ferricoxide.nast.ll.LLValue;
import org.bytedeco.llvm.global.LLVM;

public class LLIntConst extends LLExpression {

    public final int constant;

    public LLIntConst(int constant) {
        this.constant = constant;
    }

    @Override
    public void compile(LLContext ctx) {
        value = new LLValue(LLType.i32(), LLVM.LLVMConstInt(LLType.i32().ref, constant, 0));
    }

    @Override
    public String toString() {
        return constant + "i32";
    }
}
