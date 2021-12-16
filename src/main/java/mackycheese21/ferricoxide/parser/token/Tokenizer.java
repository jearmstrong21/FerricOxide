package mackycheese21.ferricoxide.parser.token;

import mackycheese21.ferricoxide.SourceCodeException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Tokenizer {

    private static int helperPWS(CodeScanner scanner) {
        int x = 0;
        while (scanner.hasNext() && Character.isWhitespace(scanner.peek().unwrapUnsafe())) {
            scanner.next();
            x++;
        }
        return x;
    }

    private static int helperPCS2(CodeScanner scanner) {
        int x = 0;
        if (scanner.hasNextSequence("//")) {
            scanner.next();
            scanner.next();
            x++;
            x++;
            while (!scanner.hasNextSequence("\n")) {
                scanner.next();
                x++;
            }
        }
        return x;
    }

    private static int helperPCS(CodeScanner scanner) {
        int x = 0;
        if (scanner.hasNextSequence("/*")) {
            scanner.next();
            scanner.next();
            x++;
            x++;
            while (!scanner.hasNextSequence("*/")) {
                scanner.next();
                x++;
            }
            scanner.next();
            scanner.next();
            x++;
            x++;
        }
        return x;
    }

    private static void purgeWhitespace(CodeScanner scanner) {
        while (true) {
            int x = helperPWS(scanner);
            int y = helperPCS(scanner);
            int z = helperPCS2(scanner);
            if (x + y + z == 0) return;
        }
    }

    private static List<Token> tokenize(String data, Path path) throws SourceCodeException {
        CodeScanner scanner = new CodeScanner(data, path);
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
                throw SourceCodeException.unexpectedChar(scanner.next().unwrapUnsafe(), scanner.latestChar());
            }
        }
        return tokens;
    }

    private static Token identifier(CodeScanner scanner) {
        String identifier = "";
        CodeScanner.Snapshot snapshot = scanner.snapshot();
        if (scanner.hasNext(Token.IDENTIFIER_START)) {
            identifier += scanner.next().unwrapUnsafe();
        } else {
            return null;
        }
        while (scanner.hasNext(Token.IDENTIFIER_REST)) {
            identifier += scanner.next().unwrapUnsafe();
        }
//        if(identifier.startsWith("#")) identifier = identifier.toLowerCase();
        for (Token.Keyword keyword : Token.Keyword.values()) {
            if (keyword.str.equals(identifier)) {
                return Token.keyword(snapshot.into(), keyword);
            }
        }
//        if(identifier.startsWith("#")) throw new RuntimeException("unrecognized preprocessor directive");
        return Token.identifier(snapshot.into(), identifier);
    }

    private static Token string(CodeScanner scanner) {
        String string = "";
        char quote;
        CodeScanner.Snapshot snapshot = scanner.snapshot();
        if (scanner.hasNext(Token.QUOTES)) {
            quote = scanner.next().unwrapUnsafe();
        } else {
            return null;
        }
        while (scanner.hasNextNot(quote + "")) {
            string += scanner.next().unwrapUnsafe();
        }
        scanner.next();
        return Token.string(snapshot.into(), string);
    }

    private static Token decimal(CodeScanner scanner) {
        Token integer = integer(scanner);
        if (integer == null) {
            return null;
        }
        if (scanner.hasNextSequence("f")) {
            scanner.next();
            return Token.decimal(integer.span, integer.integer());
        } else if (scanner.hasNextSequence(".")) {
            scanner.next();
            Token integer2 = integer(scanner);
            if (integer2 == null)
                return null;
            return Token.decimal(integer.span, Double.parseDouble(integer.integer() + "." + integer2.integer()));
            // TODO combine spans legitimately, ideally without storing too much info in Span itself
        } else {
            return null;
        }
    }

    private static int hexDigit(char c) {
        if ('0' <= c && c <= '9') return c - '0';
        if ('A' <= c && c <= 'F') return c - 'A' + 10;
        throw new UnsupportedOperationException();
    }

    private static Token integer(CodeScanner scanner) {
        int integer = 0;
        CodeScanner.Snapshot snapshot = scanner.snapshot();
        if (scanner.hasNextSequence("0x")) {
            scanner.next();
            scanner.next();
            while (scanner.hasNext("0123456789ABCDEF")) {
                integer = 16 * integer + hexDigit(scanner.next().unwrapUnsafe());
            }
            return Token.integer(snapshot.into(), integer);
        }
        if (scanner.hasNext(Token.DIGITS)) {
            integer = scanner.next().unwrapUnsafe() - '0';
        } else {
            return null;
        }
        while (scanner.hasNext(Token.DIGITS)) {
            integer = 10 * integer + (scanner.next().unwrapUnsafe() - '0');
        }
        return Token.integer(snapshot.into(), integer);
    }

    private static Token punctuation(CodeScanner scanner) {
        CodeScanner.Snapshot snapshot = scanner.snapshot();
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
                scanner.copyFrom(s);
                return Token.punctuation(snapshot.into(), p);
            }
        }
        return null;
    }

    public static List<String> INCLUDE_SEARCH_PATHS = new ArrayList<>();

    private static Path resolveFile(String name) {
        for(String str : INCLUDE_SEARCH_PATHS) {
            Path path = Path.of(str, name);
            if(path.toFile().exists()) {
                return path;
            }
        }
        throw new RuntimeException("unable to resolve file " + name);
    }

    public static List<Token> loadStr(String name) {
        Path path = resolveFile(name);
        if(includedPaths.contains(path)) return new ArrayList<>();
        includedPaths.add(path);
        try {
            return preprocess(tokenize(Files.readString(path), path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final List<Path> includedPaths = new ArrayList<>();

//    private static List<String> defines = new ArrayList<>();

    private static List<Token> preprocess(List<Token> tokens) {
        List<Token> out = new ArrayList<>();
        Iterator<Token> iter = tokens.iterator();
        while (iter.hasNext()) {
            Token next = iter.next();
            if (next.is(Token.Keyword.PP_INCLUDE)) {
                out.addAll(loadStr(iter.next().string()));
            } else {
                out.add(next);
            }
        }
        return out;
    }

}
