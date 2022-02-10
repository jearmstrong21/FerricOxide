package mackycheese21.ferricoxide.nast.hl.type;

import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HLFunctionTypeId extends HLTypeId {

    public final List<HLTypeId> params;
    public final HLTypeId result;

    public HLFunctionTypeId(Span span, List<HLTypeId> params, HLTypeId result) {
        super(span, false, false);
        this.params = params;
        this.result = result;
    }

    @Override
    public String llvmName() {
        return "F" + params.stream().map(HLTypeId::llvmName).collect(Collectors.joining("_")) + "_" + result.llvmName() + "F";
    }

    @Override
    public String toString() {
        return "fn(%s) -> %s".formatted(params.stream().map(HLTypeId::toString).collect(Collectors.joining(", ")), result);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HLFunctionTypeId that = (HLFunctionTypeId) o;
        return Objects.equals(params, that.params) && Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(params, result);
    }
}
