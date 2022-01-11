package mackycheese21.ferricoxide.ast.hl.type;

import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.parser.token.Span;

public class HLKeywordType extends HLType {

    public enum Type {
        VOID,
        BOOL,
        I8,
        I16,
        I32,
        I64,
        F32,
        F64
    }

    public final Type type;

    public HLKeywordType(Span span, Type type) {
        super(new Identifier(span, type.toString().toLowerCase()));
        this.type = type;
    }
}
