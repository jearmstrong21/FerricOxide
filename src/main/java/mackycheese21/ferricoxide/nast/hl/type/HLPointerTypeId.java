package mackycheese21.ferricoxide.nast.hl.type;

import mackycheese21.ferricoxide.parser.token.Span;

import java.util.Objects;

public class HLPointerTypeId extends HLTypeId {

    public final HLTypeId to;

    public HLPointerTypeId(Span span, HLTypeId to) {
        super(span, false, false);
        this.to = to;
    }

    @Override
    public String toString() {
        return "*" + to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HLPointerTypeId that = (HLPointerTypeId) o;
        return Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(to);
    }
}
