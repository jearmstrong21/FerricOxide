package mackycheese21.ferricoxide.nast.hl.expr;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.Pair;
import mackycheese21.ferricoxide.nast.hl.HLContext;
import mackycheese21.ferricoxide.nast.hl.HLValue;
import mackycheese21.ferricoxide.nast.hl.def.HLStructDef;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;
import mackycheese21.ferricoxide.nast.ll.LLType;
import mackycheese21.ferricoxide.nast.ll.expr.LLExpression;
import mackycheese21.ferricoxide.nast.ll.expr.LLStructInit;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HLStructInit extends HLExpression {

    public HLTypeId type;
    public final List<Pair<String, HLExpression>> fields;

    public HLStructInit(Span span, HLTypeId type, List<Pair<String, HLExpression>> fields) {
        super(span);
        this.type = type;
        this.fields = fields;
    }

    @Override
    public void compile(HLContext ctx) {
        fields.forEach(p -> {
            p.y().requireLinearFlow();
            p.y().compile(ctx);
        });
        type = ctx.resolve(type);
        HLStructDef structDef = ctx.resolveStructDef(type);
        LLType llType = ctx.compile(type);

        List<LLExpression> llFields = new ArrayList<>();
        if (fields.size() != structDef.fields.size())
            throw new AnalysisException(span, "expected %s fields, got %s".formatted(structDef.fields.size(), fields.size()));
        for (int i = 0; i < structDef.fields.size(); i++) {
            int exprIndex = -1;
            for (int j = 0; j < fields.size(); j++) {
                if (fields.get(j).x().equals(structDef.fields.get(i).x())) {
                    exprIndex = j;
                }
            }
            if (exprIndex == -1)
                throw new AnalysisException(span, "expected field %s".formatted(structDef.fields.get(i).x()));
            llFields.add(fields.get(exprIndex).y().value.ll());
        }
        value = new HLValue(type, new LLStructInit(llType, llFields));
    }

    @Override
    public String toString() {
        return "new %s { %s }".formatted(type, fields.stream().map(p -> "%s: %s".formatted(p.x(), p.y())).collect(Collectors.joining(", ")));
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
