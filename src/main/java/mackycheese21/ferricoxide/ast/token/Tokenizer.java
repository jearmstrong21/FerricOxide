package mackycheese21.ferricoxide.ast.token;

import mackycheese21.ferricoxide.ast.SourceCodeException;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

    private static void purgeWhitespace(CodeScanner scanner) {
        while (scanner.hasNext() && Character.isWhitespace(scanner.peek().unwrapUnsafe())) {
            scanner.next();
        }
    }

    public static List<Token> tokenize(String data) throws SourceCodeException {
        CodeScanner scanner = new CodeScanner(data);
        List<Token> tokens = new ArrayList<>();
        while (true) {
            purgeWhitespace(scanner);
            if (!scanner.hasNext()) break;
            CodeScanner s;

            s = scanner.copy();
            Token identifier = identifier(s);
            if (identifier != null) {
                tokens.add(identifier);
                scanner = s;
                continue;
            }

            s = scanner.copy();
            Token string = string(s);
            if (string != null) {
                tokens.add(string);
                scanner = s;
                continue;
            }

            s = scanner.copy();
            Token decimal = decimal(s);
            if (decimal != null) {
                tokens.add(decimal);
                scanner = s;
                continue;
            }

            s = scanner.copy();
            Token integer = integer(s);
            if (integer != null) {
                tokens.add(integer);
                scanner = s;
                continue;
            }

            s = scanner.copy();
            Token punctuation = punctuation(s);
            if (punctuation != null) {
                tokens.add(punctuation);
                scanner = s;
                continue;
            }

            if (scanner.hasNext()) {
                throw new SourceCodeException(SourceCodeException.Type.UNEXPECTED_CHAR, new Span(scanner.index, scanner.index + 1));
            }
        }
        return tokens;
    }

    private static Token identifier(CodeScanner scanner) {
        String identifier = "";
        int start = scanner.index;
        if (scanner.hasNext(Token.IDENTIFIER_START)) {
            identifier += scanner.next().unwrapUnsafe();
        } else {
            return null;
        }
        while (scanner.hasNext(Token.IDENTIFIER_REST)) {
            identifier += scanner.next().unwrapUnsafe();
        }
        int end = scanner.index;
        return Token.identifier(new Span(start, end), identifier);
    }

    private static Token string(CodeScanner scanner) {
        String string = "";
        String quote;
        int start = scanner.index;
        if (scanner.hasNext(Token.QUOTES)) {
            quote = scanner.next() + "";
        } else {
            return null;
        }
        while (scanner.hasNextNot(quote)) {
            string += quote;
        }
        if (scanner.hasNext(quote)) {
            scanner.next();
        } else {
            return null;
        }
        int end = scanner.index;
        return Token.string(new Span(start, end), string);
    }

    private static Token decimal(CodeScanner scanner) {
        return null;
    }

    private static Token integer(CodeScanner scanner) {
        int integer = 0;
        int start = scanner.index;
        if (scanner.hasNext(Token.DIGITS)) {
            integer = scanner.next().unwrapUnsafe() - '0';
        } else {
            return null;
        }
        while (scanner.hasNext(Token.DIGITS)) {
            integer = 10 * integer + (scanner.next().unwrapUnsafe() - '0');
        }
        int end = scanner.index;
        return Token.integer(new Span(start, end), integer);
    }

    private static Token punctuation(CodeScanner scanner) {
        int start = scanner.index;
        Token.Punctuation punctuation = null;
        for (Token.Punctuation p : Token.Punctuation.values()) {
            CodeScanner s = scanner.copy();
            boolean bad = false;
            for (int i = 0; i < p.str.length(); i++) {
                if (s.hasNext(p.str.substring(i, i + 1))) {
                    s.next();
                } else {
                    bad = true;
                }
            }
            if (!bad) {
                scanner.index = s.index;
                punctuation = p;
                break;
            }
        }
        if (punctuation == null) return null;
        int end = scanner.index;
        return Token.punctuation(new Span(start, end), punctuation);
    }

}
