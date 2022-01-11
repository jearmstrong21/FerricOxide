package mackycheese21.ferricoxide.ast.hl.mod;

import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;

public class ModDef extends ModItem {

    public final String name;
    public final List<ModItem> items;

    public ModDef(Span span, String name, List<ModItem> items) {
        super(span);
        this.name = name;
        this.items = items;
    }

    @Override
    public <T> T visit(ModItemVisitor<T> visitor) {
        return visitor.visitModDef(this);
    }
}
