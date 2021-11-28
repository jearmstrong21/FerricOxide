package mackycheese21.ferricoxide;

import mackycheese21.ferricoxide.ast.ConcreteType;
import mackycheese21.ferricoxide.ast.UnaryOperator;
import mackycheese21.ferricoxide.ast.expr.BinaryOperator;

public class AnalysisException extends RuntimeException {

    private AnalysisException(String message) {
        super(message);
    }

    public static void requireType(ConcreteType expected, ConcreteType actual) {
        if (!expected.equals(actual)) {
            throw incorrectType(expected, actual);
        }
    }

    public static void requireParamCount(int expected, int actual) {
        if (expected != actual) {
            throw incorrectParamCount(expected, actual);
        }
    }

    public static AnalysisException functionMustHaveReturn() {
        return new AnalysisException("function must have return");
    }

    public static AnalysisException cannotOverloadFunction() {
        return new AnalysisException("cannot overload function");
    }

    public static AnalysisException cannotCombineInlineExtern() {
        return new AnalysisException("cannot combine inline and extern");
    }

    public static AnalysisException noSuchKey(String key) {
        return new AnalysisException(String.format("no such key %s", key));
    }

    public static AnalysisException keyAlreadyExists(String key) {
        return new AnalysisException(String.format("key already exists %s", key));
    }

    public static AnalysisException incorrectType(ConcreteType expected, ConcreteType actual) {
        return new AnalysisException(String.format("expected %s, actual %s", expected, actual));
    }

    public static AnalysisException cannotApplyBinaryOperator(BinaryOperator operator, ConcreteType operand) {
        return new AnalysisException(String.format("cannot apply %s to %s", operator, operand));
    }

    public static AnalysisException cannotApplyUnaryOperator(UnaryOperator operator, ConcreteType operand) {
        return new AnalysisException(String.format("cannot apply %s to %s", operand, operand));
    }

    public static AnalysisException cannotAssignValue() {
        return new AnalysisException("cannot assign value");
    }

    public static AnalysisException incorrectParamCount(int expected, int actual) {
        return new AnalysisException("expected %s params, got %s".formatted(expected, actual));
    }

}
