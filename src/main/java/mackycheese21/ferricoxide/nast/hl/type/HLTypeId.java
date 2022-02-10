package mackycheese21.ferricoxide.nast.hl.type;

import mackycheese21.ferricoxide.nast.ll.LLType;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.ArrayList;
import java.util.Objects;

public abstract class HLTypeId {

    public final Span span;
    public final boolean integerType, floatType;

    protected HLTypeId(Span span, boolean integerType, boolean floatType) {
        this.span = span;
        this.integerType = integerType;
        this.floatType = floatType;
    }

    public abstract String llvmName();

    public final static class Primitive extends HLTypeId {

        @Override
        public String llvmName() {
            return name;
        }

        private final String name;
        public final LLType type;

        private Primitive(Span span, String name, LLType type) {
            // regex > passing extra parameters
            super(span,
                    name.matches("^(bool|[ui][0-9][0-9]?)$"),
                    name.matches("^f[0-9][0-9]?$")
            );
            this.name = name;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Primitive primitive = (Primitive) o;
            return Objects.equals(name, primitive.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static HLTypeId u8(Span span) {
        return new Primitive(span, "u8", LLType.u8());
    }

    public static HLTypeId i32(Span span) {
        return new Primitive(span, "i32", LLType.i32());
    }

    public static HLTypeId u32(Span span) {
        return new Primitive(span, "u32", LLType.u32());
    }

    public static HLTypeId f32(Span span) {
        return new Primitive(span, "f32", LLType.f32());
    }

    public static HLTypeId none(Span span) {
        return new HLTupleTypeId(span, new ArrayList<>());
    }

//    public void require(HLTypeId other) {
//        pred().require(other);
//    }

    public HLTypePredicate<HLTypeId> pred() {
        return HLTypePredicate.from(this);
    }

    @Override
    public abstract String toString();

    @Override
    public abstract boolean equals(Object other);

    @Override
    public abstract int hashCode();

}
