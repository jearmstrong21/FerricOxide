package mackycheese21.ferricoxide.ast.hl.type;

import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.parser.token.Span;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HLType {

    /*
    Only doesn't exist for pointer, tuple, and function types
     */
    public final @Nullable Identifier identifier;
    public final Span span;

    public HLType(Span span) {
        this.identifier = null;
        this.span = span;
    }

    public HLType(@NotNull Identifier identifier) {
        this.identifier = identifier;
        this.span = identifier.span;
    }
}
