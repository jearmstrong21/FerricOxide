package mackycheese21.ferricoxide.ast.hl.type;

import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.hl.compile.HLTypeLookup;
import mackycheese21.ferricoxide.ast.ll.type.LLType;
import org.jetbrains.annotations.NotNull;

public class HLTraitType extends HLType {
    public HLTraitType(@NotNull Identifier identifier) {
        super(identifier);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String llvmName() {
        return "impl_" + identifier.toLLVMString();
    }
}
