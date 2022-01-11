package mackycheese21.ferricoxide.ast.ll.type;

import java.util.Objects;

public class LLPointerType extends LLType {

    public LLType to;

    public LLPointerType(LLType to) {
        this.to = to;
    }

    @Override
    public <T> T visit(LLTypeVisitor<T> visitor) {
        return visitor.visitPointer(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LLPointerType that = (LLPointerType) o;
        return Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(to);
    }
}
