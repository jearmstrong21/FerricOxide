package mackycheese21.ferricoxide.ast.ll.type;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LLFunctionType extends LLType {

    public final List<LLType> params;
    public final LLType result;

    public LLFunctionType(List<LLType> params, LLType result) {
        this.params = params;
        this.result = result;
        Objects.requireNonNull(result);
    }

    @Override
    public <T> T visit(LLTypeVisitor<T> visitor) {
        return visitor.visitFunction(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LLFunctionType that = (LLFunctionType) o;
        return Objects.equals(params, that.params) && Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(params, result);
    }
}
