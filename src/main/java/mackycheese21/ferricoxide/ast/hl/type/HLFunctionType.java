package mackycheese21.ferricoxide.ast.hl.type;

import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.hl.compile.HLTypeLookup;
import mackycheese21.ferricoxide.ast.ll.type.LLFunctionType;
import mackycheese21.ferricoxide.ast.ll.type.LLType;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HLFunctionType extends HLType {

    public final List<HLType> params;
    public final HLType result;

    public HLFunctionType(Span span, List<HLType> params, HLType result) {
        super(span);
        this.params = params;
        this.result = result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HLFunctionType that = (HLFunctionType) o;
        return Objects.equals(params, that.params) && Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(params, result);
    }

    @Override
    public String llvmName() {
        return "fn_%s_%s".formatted(params.stream().map(HLType::llvmName).collect(Collectors.joining("_")), result.llvmName());
    }

    @Override
    public HLFunctionType requireFunction(Span span) {
        return this;
    }
}
