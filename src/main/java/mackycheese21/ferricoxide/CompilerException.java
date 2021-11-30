package mackycheese21.ferricoxide;

import mackycheese21.ferricoxide.ast.type.TypeReference;

public class CompilerException extends RuntimeException {

    private CompilerException(String message) {
        super(message);
    }


    public static CompilerException readOnlyTypeValidator() {
        return new CompilerException("read only type validator");
    }

    public static CompilerException moduleVerifyError(String msg) {
        return new CompilerException("LLVM module verify error:\n" + msg);
    }

    public static CompilerException unexpectedTypeReference(TypeReference reference) {
        return new CompilerException("unexpected type reference " + reference);
    }

}
