package mackycheese21.ferricoxide;

import org.bytedeco.llvm.LLVM.LLVMValueRef;

public class Variables extends IdentifierMap<Variables.Entry> {

    public static final class Entry {
        public final LLVMValueRef valueRef;
        public final ConcreteType type;

        public Entry(LLVMValueRef valueRef, ConcreteType type) {
            this.valueRef = valueRef;
            this.type = type;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "valueRef=" + valueRef +
                    ", type=" + type +
                    '}';
        }
    }

    private final Function currentFunction;

    public Variables(Function currentFunction) {
        super(null);
        this.currentFunction = currentFunction;
    }

    public Function getCurrentFunction() {
        return currentFunction;
    }

}
