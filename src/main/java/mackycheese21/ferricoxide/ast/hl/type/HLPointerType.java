package mackycheese21.ferricoxide.ast.hl.type;

import mackycheese21.ferricoxide.parser.token.Span;

public class HLPointerType extends HLType {

    public final HLType to;

    public HLPointerType(Span span, HLType to) {
        super(span);
        this.to = to;
    }
}
