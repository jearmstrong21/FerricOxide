package mackycheese21.ferricoxide.ast.hl.mod;

import mackycheese21.ferricoxide.Pair;
import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.hl.type.HLType;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;

public class StructDef extends ModItem {

    public final Identifier identifier;
    public final List<Pair<String, HLType>> fields;

    public StructDef(Span span, Identifier identifier, List<Pair<String, HLType>> fields) {
        super(span);
        this.identifier = identifier;
        this.fields = fields;
    }

    @Override
    public <T> T visit(ModItemVisitor<T> visitor) {
        return visitor.visitStructDef(this);
    }
}
