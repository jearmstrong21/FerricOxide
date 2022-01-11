package mackycheese21.ferricoxide.ast.hl.mod;

import mackycheese21.ferricoxide.parser.token.Span;

public abstract class ModItem {

    public final Span span;

    protected ModItem(Span span) {
        this.span = span;
    }

    public abstract <T> T visit(ModItemVisitor<T> visitor);

}
