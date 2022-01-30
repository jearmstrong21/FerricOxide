package mackycheese21.ferricoxide.nast.hl.def;

import mackycheese21.ferricoxide.Identifier;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;
import mackycheese21.ferricoxide.parser.token.Span;

public class HLTypedefDef implements HLModItem {

    public final Span span;
    public final Identifier name; // full path
    public final HLTypeId typeId;

    public HLTypedefDef(Span span, Identifier name, HLTypeId typeId) {
        this.span = span;
        this.name = name;
        this.typeId = typeId;
    }
}
