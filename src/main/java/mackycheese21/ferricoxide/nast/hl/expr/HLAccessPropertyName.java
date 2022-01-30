package mackycheese21.ferricoxide.nast.hl.expr;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.Pair;
import mackycheese21.ferricoxide.nast.hl.HLContext;
import mackycheese21.ferricoxide.nast.hl.HLValue;
import mackycheese21.ferricoxide.nast.hl.def.HLStructDef;
import mackycheese21.ferricoxide.nast.hl.type.HLPointerTypeId;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;
import mackycheese21.ferricoxide.nast.ll.expr.LLAccessStructIndex;
import mackycheese21.ferricoxide.parser.token.Span;

public class HLAccessPropertyName extends HLExpression {

    public final HLExpression object;
    public final String field;

    public HLAccessPropertyName(Span span, HLExpression object, String field) {
        super(span);
        this.object = object;
        this.field = field;
    }

    @Override
    public void compile(HLContext ctx) {
        object.compile(ctx);
        // TODO when to resolve typedefs
        HLStructDef struct = ctx.resolveStructDef(object.value.type());
        for (int i = 0; i < struct.fields.size(); i++) {
            Pair<String, HLTypeId> field = struct.fields.get(i);
            if (field.x().equals(this.field)) {
                value = new HLValue(new HLPointerTypeId(span, field.y()), new LLAccessStructIndex(object.value.ll(), i));
                return;
            }
        }
        throw new AnalysisException(span, "no field %s on struct %s".formatted(field, struct.name));
    }

    @Override
    public String toString() {
        return "%s.%s".formatted(object, field);
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
