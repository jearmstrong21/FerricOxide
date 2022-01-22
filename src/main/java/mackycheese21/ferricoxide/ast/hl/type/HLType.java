package mackycheese21.ferricoxide.ast.hl.type;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.parser.token.Span;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class HLType {

    /*
    Only doesn't exist for pointer, tuple, and function types
     */
    public final @Nullable Identifier identifier;
    public final @Nullable Span span;

    public HLType(@Nullable Span span) {
        this.identifier = null;
        this.span = span;
    }

    public HLType(@NotNull Identifier identifier) {
        this.identifier = identifier;
        this.span = identifier.span;
    }

    public abstract String llvmName();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HLType hlType = (HLType) o;
        return Objects.equals(identifier, hlType.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

    @Override
    public String toString() {
        if(identifier == null) throw new UnsupportedOperationException(getClass().toString());
        return identifier.toString();
    }

    public HLPointerType requirePointer(Span span) {
        throw new AnalysisException(span, "expected pointer type, got %s".formatted(toString()));
    }

    public HLFunctionType requireFunction(Span span) {
        throw new AnalysisException(span, "expected function type, got %s".formatted(toString()));
    }

    public HLStructType requireStruct(Span span) {
        throw new AnalysisException(span, "expected struct type, got %s".formatted(toString()));
    }
}
