package mackycheese21.ferricoxide.ast.hl.mod;

import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.parser.token.Span;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ImplDef extends ModItem {

    public final Identifier structFor;
    public final @Nullable Identifier trait;
    public final List<ImplFunction> functions;

    public ImplDef(Span span, Identifier structFor, @Nullable Identifier trait, List<ImplFunction> functions) {
        super(span);
        this.structFor = structFor;
        this.trait = trait;
        this.functions = functions;
    }

    @Override
    public <T> T visit(ModItemVisitor<T> visitor) {
        return visitor.visitImplDef(this);
    }
}
