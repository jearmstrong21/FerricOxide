package mackycheese21.ferricoxide.nast.hl.expr;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.Pair;
import mackycheese21.ferricoxide.nast.hl.HLContext;
import mackycheese21.ferricoxide.nast.hl.HLValue;
import mackycheese21.ferricoxide.nast.hl.def.HLImplFunctionDef;
import mackycheese21.ferricoxide.nast.hl.type.HLFunctionTypeId;
import mackycheese21.ferricoxide.nast.hl.type.HLPointerTypeId;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;
import mackycheese21.ferricoxide.nast.hl.type.HLTypePredicate;
import mackycheese21.ferricoxide.nast.ll.expr.LLAccessFunction;
import mackycheese21.ferricoxide.nast.ll.expr.LLCall;
import mackycheese21.ferricoxide.nast.ll.expr.LLExpression;
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

        LLExpression functionLL = null;
        List<HLTypeId> validateParams = null;
        HLTypeId result = null;

//        function.compile(ctx);
        if (function instanceof HLAccessPropertyName apn) {
            String fnName = apn.field;
            HLExpression object = apn.object;
            object.compile(ctx);

            HLTypeId derefObjectType = object.require(ctx, HLTypePredicate.POINTER).to;
            if(object.attempt(ctx, HLTypePredicate.pointerToHasFunctionName(fnName)) != null) {
                HLImplFunctionDef functionDef = ctx.typeMethods.get(derefObjectType).functions.get(fnName);
                functionLL = new LLAccessFunction(derefObjectType.llvmName() + "_" + fnName);
                validateParams = functionDef.proto().params.stream().map(Pair::y).collect(Collectors.toList());
                validateParams.add(0, object.value.type());
                params.add(0, object);
                result = functionDef.proto().result;
            }
        }

        if (functionLL == null) {
            function.compile(ctx);
            functionLL = function.value.ll();
            HLFunctionTypeId functionType = function.require(ctx, HLTypePredicate.FUNCTION);
            validateParams = functionType.params;
            result = functionType.result;
        }


        params.forEach(e -> e.compile(ctx));

//        HLFunctionTypeId functionType = HLTypePredicate.FUNCTION.require(function.value.type());
        if (validateParams.size() != params.size())
            throw new AnalysisException(span, "expected %s arguments, got %s".formatted(validateParams.size(), params.size()));
        for (int i = 0; i < params.size(); i++) {
            params.get(i).require(ctx, validateParams.get(i).pred());
        }
        value = new HLValue(result, new LLCall(functionLL, params.stream().map(p -> p.value.ll()).collect(Collectors.toList())));

        params.remove(0);
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
