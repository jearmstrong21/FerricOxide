package mackycheese21.ferricoxide.ast.type;

import mackycheese21.ferricoxide.ast.Identifier;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;

import java.util.List;

import static org.bytedeco.llvm.global.LLVM.*;

public class StructType extends ConcreteType {

    public final Identifier identifier;
    public final List<String> fieldNames;
    public final List<ConcreteType> fieldTypes;
    public final boolean packed;

    public StructType(Identifier identifier, List<String> fieldNames, List<ConcreteType> fieldTypes, boolean packed) {
        super(LLVMStructCreateNamed(LLVMGetGlobalContext(), identifier.toString()), false, true, identifier.toString());
        this.identifier = identifier;
        this.fieldNames = fieldNames;
        this.fieldTypes = fieldTypes;
        this.packed = packed;
    }

    public void resolved() {
        PointerPointer<LLVMTypeRef> elementTypes = new PointerPointer<>(fieldTypes.size());
        for (int i = 0; i < fieldTypes.size(); i++) {
            elementTypes.put(i, fieldTypes.get(i).typeRef);
        }
        LLVMStructSetBody(typeRef, elementTypes, fieldTypes.size(), packed ? 1 : 0);
    }

    @Override
    public String toString() {
        return "StructType[%s]".formatted(name);
    }

    @Override
    public ConcreteType getFieldType(String fieldName) {
        for (int i = 0; i < fieldNames.size(); i++) {
            if (fieldNames.get(i).equals(fieldName)) {
                return fieldTypes.get(i);
            }
        }
        return null;
    }

    @Override
    public int getFieldIndex(String fieldName) {
        return fieldNames.indexOf(fieldName);
    }
}
