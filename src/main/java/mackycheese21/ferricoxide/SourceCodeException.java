package mackycheese21.ferricoxide;

import mackycheese21.ferricoxide.parser.token.Span;

public class
SourceCodeException extends RuntimeException {

    public final Span span;

    public SourceCodeException(Span span, String message) {
        super(message);
        this.span = span;
    }


}
