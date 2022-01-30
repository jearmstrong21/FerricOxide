package mackycheese21.ferricoxide.nast.hl.def;

import mackycheese21.ferricoxide.Pair;
import mackycheese21.ferricoxide.Identifier;
import mackycheese21.ferricoxide.nast.hl.expr.HLExpression;
import mackycheese21.ferricoxide.nast.hl.type.HLFunctionTypeId;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;
import java.util.stream.Collectors;

public class HLFunctionDef implements HLModItem {

    public final Span span;
    public final Identifier name; // full path
    public final String llvmName; // null = private function
    public final List<Pair<String, HLTypeId>> params;
    public final HLTypeId result;
    public final HLExpression body;
    public final boolean export;

    public HLFunctionDef(Span span, Identifier name, String llvmName, List<Pair<String, HLTypeId>> params, HLTypeId result, HLExpression body, boolean export) {
        this.span = span;
        this.name = name;
        this.llvmName = llvmName;
        this.params = params;
        this.result = result;
        this.body = body;
        this.export = export;
    }

    public HLTypeId typeId() {
        return new HLFunctionTypeId(name.span, params.stream().map(Pair::y).collect(Collectors.toList()), result);
    }
}
