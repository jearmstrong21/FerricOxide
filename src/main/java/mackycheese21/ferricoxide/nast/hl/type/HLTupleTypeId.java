package mackycheese21.ferricoxide.nast.hl.type;

import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HLTupleTypeId extends HLTypeId {

    public final List<HLTypeId> values;

    public HLTupleTypeId(Span span, List<HLTypeId> values) {
        super(span, false, false);
        this.values = values;
    }


    @Override
    public String toString() {
        return "(%s)".formatted(values.stream().map(HLTypeId::toString).collect(Collectors.joining(", ")));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HLTupleTypeId hlTupleTypeId = (HLTupleTypeId) o;
        return Objects.equals(values, hlTupleTypeId.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }
}
