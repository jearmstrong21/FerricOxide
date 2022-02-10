package mackycheese21.ferricoxide.nast.hl.type;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.nast.hl.HLContext;
import mackycheese21.ferricoxide.nast.hl.HLValue;

public class HLTypePredicate<T> {

    private final String name;
    // an endofunctor is a monad in the category of monoids
    private final Endofunctor<T> predicate;

    private interface Endofunctor<T> {
        T run(HLContext ctx, HLTypeId t);
    }

    private HLTypePredicate(String name, Endofunctor<T> predicate) {
        this.name = name;
        this.predicate = predicate;
    }

    public static HLTypePredicate<HLTypeId> from(HLTypeId type) {
        return new HLTypePredicate<>("expected " + type.toString(), (ctx, t) -> t.equals(type) ? t : null);
    }

    public static HLTypePredicate<HLTypeId> pointerToHasFunctionName(String name) {
        return new HLTypePredicate<>("has_fn(" + name + ")", (ctx, t) -> {
            if (t instanceof HLPointerTypeId ptr) {
                if (ctx.typeMethods.containsKey(ptr.to) && ctx.typeMethods.get(ptr.to).functions.containsKey(name)) {
                    return ptr.to;
                }
            }
            return null;
        });
    }

    public static HLTypePredicate<HLPointerTypeId> POINTER = new HLTypePredicate<>("expected pointer", (ctx, t) -> t instanceof HLPointerTypeId p ? p : null);
    public static HLTypePredicate<HLFunctionTypeId> FUNCTION = new HLTypePredicate<>("expected function", (ctx, t) -> t instanceof HLFunctionTypeId f ? f : null);
    public static HLTypePredicate<HLTypeId> INT_OR_FLOAT = new HLTypePredicate<>("int or float", (ctx, t) -> t.floatType || t.integerType ? t : null);
    public static HLTypePredicate<HLTypeId> INT = new HLTypePredicate<>("int", (ctx, t) -> t.integerType ? t : null);

    public T apply(HLContext ctx, HLTypeId id) {
        return predicate.run(ctx, id);
    }

    public T require(HLContext ctx, HLTypeId id) {
        T value = predicate.run(ctx, id);
        if (value == null) throw new AnalysisException(id.span, name + ", got " + id);
        return value;
    }

    public void require(HLContext ctx, HLValue value) {
        require(ctx, value.type());
    }

    @Override
    public String toString() {
        return name;
    }

}
