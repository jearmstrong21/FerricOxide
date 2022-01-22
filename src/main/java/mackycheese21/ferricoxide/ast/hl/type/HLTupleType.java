package mackycheese21.ferricoxide.ast.hl.type;

import mackycheese21.ferricoxide.ast.hl.compile.HLTypeLookup;
import mackycheese21.ferricoxide.ast.ll.type.LLType;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HLTupleType extends HLType {

    public final List<HLType> types;

    public HLTupleType(Span span, List<HLType> types) {
        super(span);
        this.types = types;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HLTupleType that = (HLTupleType) o;
        return Objects.equals(types, that.types);
    }

    @Override
    public int hashCode() {
        return Objects.hash(types);
    }

    @Override
    public String llvmName() {
        return "tuple_%s".formatted(types.stream().map(HLType::llvmName).collect(Collectors.joining("_")));
    }
}
