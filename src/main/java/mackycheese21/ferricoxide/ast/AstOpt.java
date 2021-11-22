package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.ast.token.TokenScanner;

import java.util.Objects;

public class AstOpt<T> {

    private final T value;
    private final SourceCodeException sourceCodeException;

    private AstOpt(T value, SourceCodeException sourceCodeException) {
        this.value = value;
        this.sourceCodeException = sourceCodeException;
    }

    public static <T> AstOpt<T> empty() {
        return new AstOpt<>(null, null);
    }

    public static <T> AstOpt<T> value(T value) {
        return new AstOpt<>(Objects.requireNonNull(value), null);
    }

    public static <T> AstOpt<T> error(SourceCodeException sourceCodeException) {
        return new AstOpt<>(null, sourceCodeException);
    }

    public interface OptProducer<T> {
        T get() throws SourceCodeException;
    }

    public AstOpt<T> suppressToNull() {
        if(sourceCodeException != null) return empty();
        return this;
    }

    public static <T> AstOpt<T> from(OptProducer<T> producer) {
        try {
            return value(producer.get());
        } catch (SourceCodeException sourceCodeException) {
            return error(sourceCodeException);
        }
    }

    public static <T> AstOpt<T> fromNullable(OptProducer<T> producer) {
        try {
            T value = producer.get();
            if (value == null) {
                return empty();
            } else {
                return value(value);
            }
        } catch (SourceCodeException sourceCodeException) {
            return error(sourceCodeException);
        }
    }

    public T unwrap(TokenScanner scanner) throws SourceCodeException {
        if (sourceCodeException != null) throw sourceCodeException;
        if (value == null) {
            if(scanner == null) throw new NullPointerException();
            else throw new SourceCodeException(SourceCodeException.Type.EMPTY_UNWRAP, scanner);
        }
        return value;
    }

    @Deprecated
    public T unwrapUnsafe() {
        if(sourceCodeException != null) throw new RuntimeException(sourceCodeException);
        if(value != null) return value;
        throw new NullPointerException();
    }

    public void propogateError() throws SourceCodeException {
        if (sourceCodeException != null) throw sourceCodeException;
    }

    public boolean isPresent() {
        return value != null && sourceCodeException == null;
    }

    @Override
    public String toString() {
        return "AstOpt{" +
                "value=" + value +
                ", sourceCodeException=" + sourceCodeException +
                '}';
    }
}
