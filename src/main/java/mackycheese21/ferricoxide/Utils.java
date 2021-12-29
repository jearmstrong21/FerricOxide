package mackycheese21.ferricoxide;

import mackycheese21.ferricoxide.ast.expr.CastOperator;
import mackycheese21.ferricoxide.ast.expr.Expression;
import mackycheese21.ferricoxide.ast.type.FOType;
import mackycheese21.ferricoxide.ast.type.FunctionType;
import mackycheese21.ferricoxide.ast.type.PointerType;
import mackycheese21.ferricoxide.ast.type.UnresolvedType;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.Objects;

public class Utils {

    public static void assertTrue(boolean b) {
        if (!b) throw new AssertionError();
    }

    public static void assertFalse(boolean b) {
        if (b) throw new AssertionError();
    }

    public static void assertEquals(Object a, Object b) {
        assertTrue(Objects.equals(a, b));
    }

    public static PointerType expectPointer(Span span, FOType type) {
        if(type instanceof PointerType pointer) return pointer;
        throw new AnalysisException(span, "expected [[pointer type]], actual %s".formatted(type));
    }

    public static FunctionType expectFunction(Span span, FOType type) {
        if(type instanceof FunctionType function) return function;
        throw new AnalysisException(span, "expected [[function type]], actual %s".formatted(type));
    }

    public static void requireType(Span span, FOType expected, FOType actual) {
//        assertFalse(expected instanceof UnresolvedType);
//        assertFalse(actual instanceof UnresolvedType);
        if(expected instanceof UnresolvedType) throw new AnalysisException(span, "unresolved: expected");
        if(actual instanceof UnresolvedType) throw new AnalysisException(span, "unresolved: actual");
        if(!expected.equals(actual)) {
            throw new AnalysisException(span, "expected %s, actual %s".formatted(expected, actual));
        }
    }

    public static void requireType(FOType expected, Expression actual) {
        if(expected instanceof UnresolvedType) throw new AnalysisException(actual.span, "unresolved: expected");
        if(actual.result instanceof UnresolvedType) throw new AnalysisException(actual.span, "unresolved: actual");
        if(!expected.equals(actual.result)) {
            throw new AnalysisException(actual.span, "expected %s, actual %s (%s)".formatted(expected, actual.result, actual));
        }
    }

//    public static void requireType(Span span, FOType expected, FOType actual) {
//        assertFalse(expected instanceof UnresolvedType);
//        assertFalse(actual instanceof UnresolvedType);
//        if (!expected.equals(actual)) {
//            throw new AnalysisException(span, "expected %s, actual %s".formatted(expected, actual));
//        }
//    }

}
