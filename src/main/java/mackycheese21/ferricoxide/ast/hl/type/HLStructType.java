package mackycheese21.ferricoxide.ast.hl.type;

import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.hl.compile.HLTypeLookup;
import mackycheese21.ferricoxide.ast.ll.type.LLType;
import mackycheese21.ferricoxide.parser.token.Span;
import org.jetbrains.annotations.NotNull;

public class HLStructType extends HLType {
    public HLStructType(@NotNull Identifier identifier) {
        super(identifier);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String llvmName() {
        return identifier.toLLVMString();
    }

    @Override
    public HLStructType requireStruct(Span span) {
        return this;
    }
}
