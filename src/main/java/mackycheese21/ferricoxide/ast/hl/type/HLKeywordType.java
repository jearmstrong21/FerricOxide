package mackycheese21.ferricoxide.ast.hl.type;

import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.hl.compile.HLTypeLookup;
import mackycheese21.ferricoxide.ast.ll.type.LLPrimitiveType;
import mackycheese21.ferricoxide.ast.ll.type.LLType;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.Objects;

public class HLKeywordType extends HLType {

    public enum Type {
        VOID(LLPrimitiveType.VOID),
        BOOL(LLPrimitiveType.BOOL),
        I8(LLPrimitiveType.I8),
        I16(LLPrimitiveType.I16),
        I32(LLPrimitiveType.I32),
        I64(LLPrimitiveType.I64),
        F32(LLPrimitiveType.F32),
        F64(LLPrimitiveType.F64);

        public final LLType compile;

        Type(LLType compile) {
            this.compile = compile;
        }
    }

    public final Type type;

    public HLKeywordType(Span span, Type type) {
        super(new Identifier(span, type.toString().toLowerCase()));
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HLKeywordType that = (HLKeywordType) o;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public String llvmName() {
        return type.toString().toLowerCase();
    }

    @Override
    public String toString() {
        return type.toString().toLowerCase();
    }
}
