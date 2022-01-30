package mackycheese21.ferricoxide;

import mackycheese21.ferricoxide.parser.token.Span;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnalysisException extends RuntimeException {

    public Span span;

    public AnalysisException(@NotNull Span span, String message) {
        super(message);
        this.span = span;
    }
}
