package mackycheese21.ferricoxide.nast.ll.expr;

import mackycheese21.ferricoxide.nast.ll.LLContext;
import mackycheese21.ferricoxide.nast.ll.LLType;
import mackycheese21.ferricoxide.nast.ll.LLValue;
import org.bytedeco.llvm.global.LLVM;

public class LLAccessStructIndex extends LLExpression {

    public final LLExpression object;
    public final int index;

    public LLAccessStructIndex(LLExpression object, int index) {
        this.object = object;
        this.index = index;
    }

    // TODO: somewhere there is an inconsistency in my lang
    // makes sense llvm can't index directly to a struct
    // but why is there a struct just sitting around like that
    // for returned structs like this it should be put onto the stack
    // is this a special case? I dont think so: (fn() -> bar)() returns the same llvm value as new bar { x: 5 }
    // So HL access struct index should compile this to an anonymous alloca I think
    // that seems to be what C compiler does? test with inline initialization (printInt(new bar{}.x) vs printInt(foo().x)) just to make sure

    // "&" operator for anything that takes X -> X* by storing it
    // implicit "&" operator for access

    @Override
    public void compile(LLContext ctx) {
        object.compile(ctx);
        value = new LLValue(LLType.pointer(object.value.type().pointerDeref.fields.get(index)), LLVM.LLVMBuildStructGEP2(ctx.builder(), object.value.type().pointerDeref.ref, object.value.ref(), index, "gep"));
    }

    @Override
    public String toString() {
        return "%s.%s".formatted(object, index);
    }
}
