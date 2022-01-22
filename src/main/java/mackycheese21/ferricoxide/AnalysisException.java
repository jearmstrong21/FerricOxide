package mackycheese21.ferricoxide;

import mackycheese21.ferricoxide.parser.token.Span;
import org.jetbrains.annotations.Nullable;

public class AnalysisException extends RuntimeException {

    public Span span;

    public AnalysisException(@Nullable Span span, String message) {
        super(message);
        if(span == null) throw new UnsupportedOperationException("compiler error");
        this.span = span;
    }
}
