package mackycheese21.ferricoxide.ast.hl.type;

import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;

public class HLFunctionType extends HLType {

    public final List<HLType> params;
    public final HLType result;

    public HLFunctionType(Span span, List<HLType> params, HLType result) {
        super(span);
        this.params = params;
        this.result = result;
    }
}
