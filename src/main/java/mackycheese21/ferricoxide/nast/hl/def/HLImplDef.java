package mackycheese21.ferricoxide.nast.hl.def;

import mackycheese21.ferricoxide.nast.hl.type.HLIdentifierTypeId;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HLImplDef implements HLModItem {

    public final HLIdentifierTypeId struct;
    public final @Nullable HLIdentifierTypeId trait;
    public final List<HLImplFunctionDef> functions;

    public HLImplDef(HLIdentifierTypeId struct, @Nullable HLIdentifierTypeId trait, List<HLImplFunctionDef> functions) {
        this.struct = struct;
        this.trait = trait;
        this.functions = functions;
    }
}
