package mackycheese21.ferricoxide.nast.hl.def;

import mackycheese21.ferricoxide.Pair;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;

import java.util.List;

public class HLImplFunctionPrototype {

    public enum Self {
        NONE,
        VALUE,
        REF
    }

    public final String name;
    public final Self self;
    public final List<Pair<String, HLTypeId>> params;

    public HLImplFunctionPrototype(String name, Self self, List<Pair<String, HLTypeId>> params) {
        this.name = name;
        this.self = self;
        this.params = params;
    }
}
