package mackycheese21.ferricoxide.ast.hl.type;

import mackycheese21.ferricoxide.ast.hl.compile.HLTypeLookup;
import mackycheese21.ferricoxide.ast.ll.type.LLPointerType;
import mackycheese21.ferricoxide.ast.ll.type.LLType;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.Objects;

public class HLPointerType extends HLType {

    public final HLType to;

    public HLPointerType(Span span, HLType to) {
        super(span);
        this.to = to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HLPointerType that = (HLPointerType) o;
        return Objects.equals(to, that.to);
    }

    @Override
    public String toString() {
        return "*" + to;
    }

    @Override
    public int hashCode() {
        return Objects.hash(to);
    }

    @Override
    public String llvmName() {
        return "ptr_%s".formatted(to.llvmName());
    }

    @Override
    public HLPointerType requirePointer(Span span) {
        return this;
    }
}
