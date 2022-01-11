package mackycheese21.ferricoxide.ast.hl.mod;

import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;

public class TraitDef extends ModItem {

    public final Identifier identifier;
    public final List<FunctionPrototype> prototypes;

    public TraitDef(Span span, Identifier identifier, List<FunctionPrototype> prototypes) {
        super(span);
        this.identifier = identifier;
        this.prototypes = prototypes;
    }

    @Override
    public <T> T visit(ModItemVisitor<T> visitor) {
        return visitor.visitTraitDef(this);
    }
}
