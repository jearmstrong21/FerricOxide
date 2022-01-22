package mackycheese21.ferricoxide.ast.hl.mod;

import mackycheese21.ferricoxide.Pair;
import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.hl.type.HLFunctionType;
import mackycheese21.ferricoxide.ast.hl.type.HLType;

import java.util.List;
import java.util.stream.Collectors;

public class FunctionPrototype {
    public final Identifier name;
    public final List<Pair<String, HLType>> params;
    public final HLType result;

    public FunctionPrototype(Identifier name, List<Pair<String, HLType>> params, HLType result) {
        this.name = name;
        this.params = params;
        this.result = result;
    }

    public HLFunctionType functionType() {
        return new HLFunctionType(name.span, params.stream().map(Pair::y).collect(Collectors.toList()), result);
    }
}
