package mackycheese21.ferricoxide.ast.type;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.bytedeco.llvm.global.LLVM.LLVMPointerType;

public class PointerType extends ConcreteType {

    public final ConcreteType to;

    private static final Map<ConcreteType, PointerType> registry = new HashMap<>();

    public static PointerType of(ConcreteType to) {
        if(registry.containsKey(to)) return registry.get(to);
        registry.put(to, new PointerType(to));
        return registry.get(to);
    }

    private PointerType(ConcreteType to) {
        super(to.typeRef == null ? null : LLVMPointerType(to.typeRef, 0), false, true, to.name + "*");
        this.to = to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PointerType that = (PointerType) o;
        return Objects.equals(to, that.to);
    }

//    @Override
//    public int getFieldIndex(String fieldName) {
//        return to.getFieldIndex(fieldName);
//    }
//
//    @Override
//    public ConcreteType getFieldType(String fieldName) {
//        return to.getFieldType(fieldName);
//    }

}
