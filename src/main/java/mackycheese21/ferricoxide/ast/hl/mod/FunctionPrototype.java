package mackycheese21.ferricoxide.ast.hl.mod;

import mackycheese21.ferricoxide.Pair;
import mackycheese21.ferricoxide.ast.hl.type.HLType;

import java.util.List;

public class FunctionPrototype {
    public final String name;
    public final List<Pair<String, HLType>> params;
    public final HLType result;

    public FunctionPrototype(String name, List<Pair<String, HLType>> params, HLType result) {
        this.name = name;
        this.params = params;
        this.result = result;
    }
}
