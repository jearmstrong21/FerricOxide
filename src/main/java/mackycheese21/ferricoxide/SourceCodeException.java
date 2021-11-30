package mackycheese21.ferricoxide;

import mackycheese21.ferricoxide.parser.token.Span;
import mackycheese21.ferricoxide.parser.token.Token;
import mackycheese21.ferricoxide.parser.token.TokenScanner;

import java.util.Arrays;

public class
SourceCodeException extends RuntimeException {

    public final Span span;

    private SourceCodeException(String message, Span span) {
        super(message);
        this.span = span;
    }

    public static SourceCodeException expectedType(TokenScanner scanner) {
        return new SourceCodeException("expected type", scanner.currentOrLast().span);
    }

    public static SourceCodeException expectedStatement(TokenScanner scanner) {
        return new SourceCodeException("expected statement", scanner.currentOrLast().span);
    }

    public static SourceCodeException expectedBlock(TokenScanner scanner) {
        return new SourceCodeException("expected block", scanner.currentOrLast().span);
    }

    public static SourceCodeException expectedSimple(TokenScanner scanner) {
        return new SourceCodeException("expected simple", scanner.currentOrLast().span);
    }

    public static SourceCodeException unexpectedChar(char ch, Span span) {
        return new SourceCodeException("unexpected char " + ch, span);
    }

    public static SourceCodeException unexpectedEOF(Token token) {
        return new SourceCodeException(String.format("unexpected eof: %s", token.valueString()), token.span);
    }

    public static SourceCodeException unexpectedEOF(Span span) {
        return new SourceCodeException("unexpected eof", span);
    }

    public static SourceCodeException expectedToken(Token token, Token.Type... types) {
        return new SourceCodeException(String.format("expected %s, got %s", Arrays.toString(types), token.valueString()), token.span);
    }

    public static SourceCodeException expectedToken(Token token, Token.Punctuation... punctuations) {
        return new SourceCodeException(String.format("expected %s, got %s", Arrays.toString(punctuations), token.valueString()), token.span);
    }

    public static SourceCodeException expectedToken(Token token, Token.Keyword... keywords) {
        return new SourceCodeException(String.format("expected %s, got %s", Arrays.toString(keywords), token), token.span);
    }

}
