package mackycheese21.ferricoxide.ast.type;

import mackycheese21.ferricoxide.ast.Identifier;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;

import static org.bytedeco.llvm.global.LLVM.*;

public class ConcreteType {

    static {
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();

        LLVMInitializeAllTargetInfos();
        LLVMInitializeAllTargets();
        LLVMInitializeAllTargetMCs();
        LLVMInitializeAllAsmParsers();
        LLVMInitializeAllAsmPrinters();
    }

    public final LLVMTypeRef typeRef;
    public final boolean numeric;
    public final boolean complete;
    public final boolean declarable;
    public final String name;

    protected ConcreteType(LLVMTypeRef typeRef, boolean numeric, boolean declarable, String name) {
        this.typeRef = typeRef;
        this.numeric = numeric;
        this.complete = typeRef != null;
        this.declarable = declarable;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Concrete[%s]".formatted(name);
    }

    private static ConcreteType intType(int width, String name) {
        return new ConcreteType(LLVMIntType(width), true, true, name);
    }

    public ConcreteType getFieldType(String fieldName) {
        return null;
    }

    public int getFieldIndex(String fieldName) {
        return -1;
    }

    public static final ConcreteType I8 = intType(8, "i8");
    public static final ConcreteType I32 = intType(32, "i32");
    public static final ConcreteType I64 = intType(64, "i64");

    public static final ConcreteType F32 = new ConcreteType(LLVMFloatType(), true, true, "f32");
    public static final ConcreteType F64 = new ConcreteType(LLVMDoubleType(), true, true, "f64");

    public static final ConcreteType BOOL = intType(1, "bool");
    public static final ConcreteType VOID = new ConcreteType(LLVMVoidType(), false, false, "void");

}
