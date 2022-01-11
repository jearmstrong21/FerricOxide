package mackycheese21.ferricoxide.ast.ll.type;

public abstract class LLType {

    public abstract <T> T visit(LLTypeVisitor<T> visitor);

    public final boolean isFloatingPoint() {
        return this == LLPrimitiveType.F32 || this == LLPrimitiveType.F64;
    }

}
