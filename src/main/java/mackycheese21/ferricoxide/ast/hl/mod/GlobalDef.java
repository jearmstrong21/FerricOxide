package mackycheese21.ferricoxide.ast.hl.mod;

import mackycheese21.ferricoxide.ast.hl.expr.HLExpression;
import mackycheese21.ferricoxide.ast.hl.type.HLType;
import mackycheese21.ferricoxide.parser.token.Span;

public class GlobalDef extends ModItem {

    public final String name;
    public final HLType type;
    public final HLExpression value;

    public GlobalDef(Span span, String name, HLType type, HLExpression value) {
        super(span);
        this.name = name;
        this.type = type;
        this.value = value;
    }

    @Override
    public <T> T visit(ModItemVisitor<T> visitor) {
        return visitor.visitGlobalDef(this);
    }
}
