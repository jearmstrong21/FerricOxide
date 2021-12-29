package mackycheese21.ferricoxide;

import mackycheese21.ferricoxide.parser.token.Span;

public class AnalysisException extends RuntimeException {

    public Span span;

    public AnalysisException(Span span, String message) {
        super(message);
        this.span = span;
    }
}
