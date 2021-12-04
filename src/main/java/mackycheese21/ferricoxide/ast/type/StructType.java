package mackycheese21.ferricoxide.ast.type;

import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;

import java.util.List;

import static org.bytedeco.llvm.global.LLVM.*;

public class StructType extends ConcreteType { // tmrw: the error is because new TypeRef("compl") != new TypeRef("compl"), need to "resolve" TypeRefs - dod so in line 171 of typevalidatevisitor? thats one place, at least. maybe a method on FOModule!! YES!! method on FOModule to resolve types, this can alsoo do stuff like template instantiation later

    public final List<String> fieldNames;
    public final List<ConcreteType> fieldTypes;
    public final boolean packed;

    public StructType(String name, List<String> fieldNames, List<ConcreteType> fieldTypes, boolean packed) {
        super(LLVMStructCreateNamed(LLVMGetGlobalContext(), name), false, true, name);
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
        System.out.println("STRUCT GETFIELDTYPE " + fieldName + " " + fieldNames);
        return fieldNames.indexOf(fieldName);
    }
}
