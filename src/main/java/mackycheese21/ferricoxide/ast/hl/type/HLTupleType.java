package mackycheese21.ferricoxide.ast.hl.type;

import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;

public class HLTupleType extends HLType {

    public final List<HLType> types;

    public HLTupleType(Span span, List<HLType> types) {
        super(span);
        this.types = types;
    }
}
