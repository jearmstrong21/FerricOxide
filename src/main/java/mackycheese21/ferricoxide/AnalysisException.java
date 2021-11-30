package mackycheese21.ferricoxide;

import mackycheese21.ferricoxide.ast.type.ConcreteType;
import mackycheese21.ferricoxide.ast.type.PointerType;
import mackycheese21.ferricoxide.ast.type.TypeReference;
import mackycheese21.ferricoxide.ast.expr.UnaryOperator;
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

    public static PointerType requirePointer(ConcreteType type) {
        if(type instanceof PointerType pointer) return pointer;
        throw new AnalysisException("expected pointer type, got " + type);
    }

    public static AnalysisException cannotDeclareType(ConcreteType type) {
        return new AnalysisException("cannot declare type " + type);
    }

    public static AnalysisException incorrectStructInitializer() {
        //TODO
        throw new AnalysisException("the error message writer guild is on strike");
    }

    public static AnalysisException noSuchField(ConcreteType object, String field) {
        return new AnalysisException("no such field %s on %s".formatted(field, object));
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
        if(actual instanceof TypeReference reference) throw CompilerException.unexpectedTypeReference(reference);
        if(expected instanceof TypeReference reference) throw CompilerException.unexpectedTypeReference(reference);
        return new AnalysisException(String.format("expected %s, actual %s", expected, actual));
    }

    public static AnalysisException noTypeDeclared(TypeReference reference) {
        return new AnalysisException("no type declared %s".formatted(reference));
    }

    public static AnalysisException cannotApplyBinaryOperator(BinaryOperator operator, ConcreteType operand) {
        return new AnalysisException(String.format("cannot apply %s to %s", operator, operand));
    }

    public static AnalysisException cannotApplyUnaryOperator(UnaryOperator operator, ConcreteType operand) {
        return new AnalysisException(String.format("cannot apply %s to %s", operator, operand));
    }

    public static AnalysisException cannotAssignValue() {
        return new AnalysisException("cannot assign value");
    }

    public static AnalysisException incorrectParamCount(int expected, int actual) {
        return new AnalysisException("expected %s params, got %s".formatted(expected, actual));
    }

    public static AnalysisException invalidCast(ConcreteType from, ConcreteType to) {
        return new AnalysisException("invalid cast from %s to %s".formatted(from, to));
    }
}
