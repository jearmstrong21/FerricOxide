package mackycheese21.ferricoxide;

import org.bytedeco.llvm.LLVM.LLVMTypeRef;

import static org.bytedeco.llvm.global.LLVM.*;

public class ConcreteType {

    private final LLVMTypeRef typeRef;
    private final boolean integer;
    private final String name;

    private ConcreteType(LLVMTypeRef typeRef, boolean integer, String name) {
        this.typeRef = typeRef;
        this.integer = integer;
        this.name = name;
    }

    public LLVMTypeRef llvmTypeRef() {
        return typeRef;
    }

    public boolean isInteger() {
        return integer;
    }

    @Override
    public String toString() {
        return name;
    }

    private static ConcreteType intType(int width, String name) {
        return new ConcreteType(LLVMIntType(width), true, name);
    }

    public static final ConcreteType I32 = intType(32, "i32");
    public static final ConcreteType I64 = intType(64, "i64");

    public static final ConcreteType F32 = new ConcreteType(LLVMFloatType(), false, "f32");
    public static final ConcreteType F64 = new ConcreteType(LLVMDoubleType(), false, "f64");

    public static final ConcreteType BOOL = intType(1, "bool");
    public static final ConcreteType NONE = intType(0, "none");

}
