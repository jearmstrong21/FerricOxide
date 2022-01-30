package mackycheese21.ferricoxide.nast.hl.def;

import mackycheese21.ferricoxide.Identifier;
import mackycheese21.ferricoxide.nast.hl.expr.HLExpression;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;
import mackycheese21.ferricoxide.parser.token.Span;

public class HLGlobalDef implements HLModItem {

    public final Span span;
    public final Identifier name;
    public final HLTypeId type;
    public final HLExpression value;

    public HLGlobalDef(Span span, Identifier name, HLTypeId type, HLExpression value) {
        this.span = span;
        this.name = name;
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return "HLGlobalDef{" +
                "span=" + span +
                ", name=" + name +
                ", type=" + type +
                ", value=" + value +
                '}';
    }
}
