package mackycheese21.ferricoxide.nast.ll.expr;

import mackycheese21.ferricoxide.nast.ll.LLContext;
import mackycheese21.ferricoxide.nast.ll.LLValue;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMValueRef;
import org.bytedeco.llvm.global.LLVM;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LLCall extends LLExpression {

    public final LLExpression function;
    public final List<LLExpression> params;

    public LLCall(LLExpression function, List<LLExpression> params) {
        this.function = function;
        this.params = params;
    }

    @Override
    public void compile(LLContext ctx) {
        function.compile(ctx);
        compileList(ctx, params);

        LLVMValueRef[] paramRefs = params.stream().map(e -> e.value.ref()).collect(Collectors.toList()).toArray(LLVMValueRef[]::new);

        value = new LLValue(Objects.requireNonNull(function.value.type().functionResult), LLVM.LLVMBuildCall2(ctx.builder(),
                function.value.type().ref,
                function.value.ref(),
                new PointerPointer<>(params.size()).put(paramRefs),
                params.size(), ""));

        if(params.size() > 0) LLVM.LLVMBuildRetVoid(ctx.builder());
    }

    @Override
    public String toString() {
        return "%s(%s)".formatted(function, params.stream().map(LLExpression::toString).collect(Collectors.joining(", ")));
    }
}
