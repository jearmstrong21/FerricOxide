package mackycheese21.ferricoxide.nast.ll.expr;

import mackycheese21.ferricoxide.nast.ll.LLContext;
import mackycheese21.ferricoxide.nast.ll.LLType;
import mackycheese21.ferricoxide.nast.ll.LLValue;
import org.bytedeco.llvm.LLVM.LLVMValueRef;
import org.bytedeco.llvm.global.LLVM;

import java.util.List;
import java.util.stream.Collectors;

public class LLStructInit extends LLExpression {

    public final LLType type;
    public final List<LLExpression> fields;

    public LLStructInit(LLType type, List<LLExpression> fields) {
        this.type = type;
        this.fields = fields;
    }

    @Override
    public void compile(LLContext ctx) {
        LLVMValueRef ref = LLVM.LLVMConstNull(type.ref);
        for (int i = 0; i < fields.size(); i++) {
            fields.get(i).compile(ctx);
            ref = LLVM.LLVMBuildInsertValue(ctx.builder(), ref, fields.get(i).value.ref(), i, "insert");
        }
        value = new LLValue(type, ref);
    }

    @Override
    public String toString() {
        return "new %s { %s }".formatted(type, fields.stream().map(LLExpression::toString).collect(Collectors.joining(", ")));
    }
}
