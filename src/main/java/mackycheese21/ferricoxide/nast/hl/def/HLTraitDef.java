package mackycheese21.ferricoxide.nast.hl.def;

import mackycheese21.ferricoxide.Identifier;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;

public class HLTraitDef implements HLModItem {

    public final Span span;
    public final Identifier name; // full path
    public final List<HLImplFunctionPrototype> functions;

    public HLTraitDef(Span span, Identifier name, List<HLImplFunctionPrototype> functions) {
        this.span = span;
        this.name = name;
        this.functions = functions;
    }
}
