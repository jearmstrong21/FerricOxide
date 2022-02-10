package mackycheese21.ferricoxide.nast.hl.def;

import mackycheese21.ferricoxide.Identifier;
import mackycheese21.ferricoxide.Pair;
import mackycheese21.ferricoxide.Utils;
import mackycheese21.ferricoxide.nast.hl.type.HLFunctionTypeId;
import mackycheese21.ferricoxide.nast.hl.type.HLPointerTypeId;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;

import java.util.ArrayList;
import java.util.List;

public class HLImplFunctionPrototype {

    public final boolean sttc;
    public final HLTypeId enclosingType;
    public final Identifier name;
    public final Identifier modPath;
    public final List<Pair<String, HLTypeId>> params;
    public final HLTypeId result;

    public HLImplFunctionPrototype(boolean sttc, HLTypeId enclosingType, Identifier name, Identifier modPath, List<Pair<String, HLTypeId>> params, HLTypeId result) {
        this.sttc = sttc;
        this.modPath = modPath;
        Utils.assertTrue(name.length() == 1);
        this.enclosingType = enclosingType;
        this.name = name;
        this.params = params;
        this.result = result;
    }

    public HLFunctionTypeId typeId() {
        List<HLTypeId> fnParams = new ArrayList<>();
        fnParams.add(new HLPointerTypeId(name.span, enclosingType)); // IMPLICIT POINTER TO SELF
        params.forEach(p -> fnParams.add(p.y()));
        return new HLFunctionTypeId(name.span, fnParams, result);
    }

}
