package mackycheese21.ferricoxide.ast;

import java.util.Objects;

public class AstOpt<T> {

    private final T value;
    private final RuntimeException exception;

    private AstOpt(T value, RuntimeException exception) {
        this.value = value;
        this.exception = exception;
    }

    public static <T> AstOpt<T> value(T value) {
        return new AstOpt<>(Objects.requireNonNull(value), null);
    }

    public static <T> AstOpt<T> error(RuntimeException exception) {
        return new AstOpt<>(null, exception);
    }

    @Deprecated
    public T unwrapUnsafe() {
        if(exception != null) throw exception;
        if(value != null) return value;
        throw new NullPointerException();
    }

    @Override
    public String toString() {
        return "AstOpt{" +
                "value=" + value +
                ", exception=" + exception +
                '}';
    }
}
