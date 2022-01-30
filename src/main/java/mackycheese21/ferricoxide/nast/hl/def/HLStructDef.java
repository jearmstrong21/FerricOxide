package mackycheese21.ferricoxide.nast.hl.def;

import mackycheese21.ferricoxide.Pair;
import mackycheese21.ferricoxide.Identifier;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;

public class HLStructDef implements HLModItem {

    public final Span span;
    public final Identifier name; // full path
    public final List<Pair<String, HLTypeId>> fields;

    public HLStructDef(Span span, Identifier name, List<Pair<String, HLTypeId>> fields) {
        this.span = span;
        this.name = name;
        this.fields = fields;
    }
}
