package mackycheese21.ferricoxide;

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

}
