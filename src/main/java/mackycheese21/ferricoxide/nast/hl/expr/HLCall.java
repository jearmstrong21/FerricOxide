package mackycheese21.ferricoxide.nast.hl.expr;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.nast.hl.HLContext;
import mackycheese21.ferricoxide.nast.hl.HLValue;
import mackycheese21.ferricoxide.nast.hl.type.HLFunctionTypeId;
import mackycheese21.ferricoxide.nast.hl.type.HLTypePredicate;
import mackycheese21.ferricoxide.nast.ll.expr.LLCall;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;
import java.util.stream.Collectors;

public class HLCall extends HLExpression {

    public final HLExpression function;
    public final List<HLExpression> params;

    public HLCall(Span span, HLExpression function, List<HLExpression> params) {
        super(span);
        this.function = function;
        this.params = params;
    }

    @Override
    public void compile(HLContext ctx) {
        function.requireLinearFlow();
        params.forEach(HLExpression::requireLinearFlow);

        function.compile(ctx);
        params.forEach(e -> e.compile(ctx));

        HLFunctionTypeId functionType = HLTypePredicate.FUNCTION.require(function.value.type());
        if (functionType.params.size() != params.size())
            throw new AnalysisException(span, "expected %s arguments, got %s".formatted(functionType.params.size(), params.size()));
        for (int i = 0; i < params.size(); i++) {
            params.get(i).require(ctx, functionType.params.get(i).pred());
        }
        value = new HLValue(functionType.result, new LLCall(function.value.ll(), params.stream().map(p -> p.value.ll()).collect(Collectors.toList())));
    }

    @Override
    public String toString() {
        return "%s(%s)".formatted(function, params.stream().map(HLExpression::toString).collect(Collectors.joining(", ")));
    }

    @Override
    public boolean hasForcedReturn() {
        return false;
    }

    @Override
    public boolean hasForcedBreak() {
        return false;
    }
}
