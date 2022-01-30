package mackycheese21.ferricoxide.nast.hl.type;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.nast.hl.HLValue;

import java.util.function.Function;

public class HLTypePredicate<T> {

    private final String name;
    private final Function<HLTypeId, T> predicate;

    private HLTypePredicate(String name, Function<HLTypeId, T> predicate) {
        this.name = name;
        this.predicate = predicate;
    }

    public static HLTypePredicate<HLTypeId> from(HLTypeId type) {
        return new HLTypePredicate<>("expected " + type.toString(), t -> t.equals(type) ? t : null);
    }

    public static HLTypePredicate<HLPointerTypeId> POINTER = new HLTypePredicate<>("expected pointer", t -> t instanceof HLPointerTypeId p ? p : null);
    public static HLTypePredicate<HLFunctionTypeId> FUNCTION = new HLTypePredicate<>("expected function", t -> t instanceof HLFunctionTypeId f ? f : null);
    public static HLTypePredicate<HLTypeId> INT_OR_FLOAT = new HLTypePredicate<>("int or float", t -> t.floatType || t.integerType ? t : null);
    public static HLTypePredicate<HLTypeId> INT = new HLTypePredicate<>("int", t -> t.integerType ? t : null);

    public T apply(HLTypeId id) {
        return predicate.apply(id);
    }

    public T require(HLTypeId id) {
        T value = predicate.apply(id);
        if (value == null) throw new AnalysisException(id.span, name + ", got " + id);
        return value;
    }

    public void require(HLValue value) {
        require(value.type());
    }

    @Override
    public String toString() {
        return name;
    }

}
