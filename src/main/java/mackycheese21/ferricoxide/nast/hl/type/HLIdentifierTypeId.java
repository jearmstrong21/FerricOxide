package mackycheese21.ferricoxide.nast.hl.type;

import mackycheese21.ferricoxide.Identifier;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.Objects;

public class HLIdentifierTypeId extends HLTypeId {

    @Override
    public String llvmName() {
        return identifier.toLLVMString();
    }

    public final Identifier identifier;

    public HLIdentifierTypeId(Span span, Identifier identifier) {
        super(span, false, false);
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        return identifier.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HLIdentifierTypeId that = (HLIdentifierTypeId) o;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }
}
