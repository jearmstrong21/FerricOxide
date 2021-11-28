package mackycheese21.ferricoxide.ast;

import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;

import java.util.List;
import java.util.stream.Collectors;

import static org.bytedeco.llvm.global.LLVM.*;

public class ConcreteType {

    private static boolean llvm = false;

    static {
        if (!llvm) {
            llvm = true;

            LLVMInitializeCore(LLVMGetGlobalPassRegistry());
            LLVMLinkInMCJIT();

            LLVMInitializeAllTargetInfos();
            LLVMInitializeAllTargets();
            LLVMInitializeAllTargetMCs();
            LLVMInitializeAllAsmParsers();
            LLVMInitializeAllAsmPrinters();
        }
    }

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

    public static class Function extends ConcreteType {
        public final ConcreteType result;
        public final List<ConcreteType> params;

        public Function(ConcreteType result, List<ConcreteType> params) {
            super(LLVMFunctionType(
                            result.llvmTypeRef(),
                            new PointerPointer<>(params.stream().map(ConcreteType::llvmTypeRef)
                                    .collect(Collectors.toList()).toArray(LLVMTypeRef[]::new)),
                            params.size(), 0 /* false */), false,
                    "%s(%s)".formatted(result, params.stream().map(ConcreteType::toString)
                            .collect(Collectors.joining(", "))));
            this.result = result;
            this.params = params;
        }
    }

    public static final ConcreteType I32 = intType(32, "i32");
    public static final ConcreteType I64 = intType(64, "i64");

    public static final ConcreteType F32 = new ConcreteType(LLVMFloatType(), false, "f32");
    public static final ConcreteType F64 = new ConcreteType(LLVMDoubleType(), false, "f64");

    public static final ConcreteType BOOL = intType(1, "bool");
    public static final ConcreteType VOID = new ConcreteType(LLVMVoidType(), false, "void");

}
