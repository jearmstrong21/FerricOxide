package mackycheese21.ferricoxide.nast.hl.def;

import mackycheese21.ferricoxide.Identifier;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;

public class HLImplDef implements HLModItem {

    public final Span span;
    public final Identifier modPath;
    public final HLTypeId typeId;
    public final List<HLImplFunctionDef> functions;

    public HLImplDef(Span span, Identifier modPath, HLTypeId typeId, List<HLImplFunctionDef> functions) {
        this.span = span;
        this.modPath = modPath;
        this.typeId = typeId;
        this.functions = functions;
    }
}
