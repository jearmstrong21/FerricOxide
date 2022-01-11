package mackycheese21.ferricoxide.ast.ll.type;

import java.util.Objects;

public final class LLPrimitiveType extends LLType {

    public final String name;

    private LLPrimitiveType(String name) {
        this.name = name;
    }

    @Override
    public <T> T visit(LLTypeVisitor<T> visitor) {
        return visitor.visitPrimitive(this);
    }

    public final static LLType VOID = new LLPrimitiveType("void");
    public final static LLType BOOL = new LLPrimitiveType("bool");
    public final static LLType I8 = new LLPrimitiveType("i8");
    public final static LLType I16 = new LLPrimitiveType("i16");
    public final static LLType I32 = new LLPrimitiveType("i32");
    public final static LLType I64 = new LLPrimitiveType("i64");
    public final static LLType F32 = new LLPrimitiveType("f32");
    public final static LLType F64 = new LLPrimitiveType("f64");

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LLPrimitiveType that = (LLPrimitiveType) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
