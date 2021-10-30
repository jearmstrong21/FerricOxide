package mackycheese21.ferricoxide;

import org.bytedeco.llvm.LLVM.LLVMTypeRef;

import static org.bytedeco.llvm.global.LLVM.*;

public abstract class ConcreteType {

    public abstract LLVMTypeRef llvmTypeRef();

    private static ConcreteType intType(int width, String name) {
        return new ConcreteType() {
            @Override
            public LLVMTypeRef llvmTypeRef() {
                return LLVMIntType(width);
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    public static final ConcreteType I32 = intType(32, "I32");
    public static final ConcreteType BOOL = intType(1, "BOOL");
    public static final ConcreteType NONE = intType(0, "NONE");

}
