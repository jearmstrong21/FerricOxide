package mackycheese21.ferricoxide.parser.token;

import mackycheese21.ferricoxide.SourceCodeException;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

    private static GroupToken attemptGroup(Scanner scanner) {
        for (GroupToken.Type type : GroupToken.Type.values()) {
            if (type.left == scanner.peek()) {
                Span.Loc start = scanner.loc();
                scanner.next();
                scanner.purge();
                List<TokenTree> values = tokenize(scanner);
                scanner.purge();
                if (scanner.next() != type.right)
                    throw new SourceCodeException(scanner.latestSpan(), "expected closing " + type);
                return new GroupToken(scanner.from(start), type, values);
            }
        }
        return null;
    }

    private static PunctToken attemptPunct(Scanner scanner) {
        for (PunctToken.Type type : PunctToken.Type.values()) {
            if (scanner.has(type.str)) {
                Span.Loc start = scanner.loc();
                for (int i = 0; i < type.str.length(); i++) scanner.next();
                return new PunctToken(scanner.from(start), type);
            }
        }
        return null;
    }

    private static LiteralToken.Integer attemptBase10Int(Scanner scanner) {
        Span.Loc start = scanner.loc();
        long value = 0;
        if (!scanner.nextIsIn("0123456789")) return null;
        while (true) {
            int digit;
            if (scanner.peek() == 'f') break;
            if (scanner.nextIsIn("0123456789")) {
                digit = scanner.next() - '0';
            } else {
                scanner.requirePunct();
                break;
            }
            value = value * 10 + digit;
        }
        return new LiteralToken.Integer(scanner.from(start), value);
    }

    private static LiteralToken.Integer attemptBase16Int(Scanner scanner) {
        if (!scanner.has("0x")) return null;
        Span.Loc start = scanner.loc();
        scanner.next(2);
        long value = 0;
        while (true) {
            int digit;
            if (scanner.nextIsIn("0123456789")) {
                digit = scanner.next() - '0';
            } else if (scanner.nextIsIn("ABCDEF")) {
                digit = scanner.next() - 'A' + 10;
            } else if (scanner.nextIsIn("abcdef")) {
                digit = scanner.next() - 'a' + 10;
            } else {
                scanner.requirePunct();
                break;
            }
            value = value * 16 + digit;
        }
        return new LiteralToken.Integer(scanner.from(start), value);
    }

    private static LiteralToken attemptNumber(Scanner scanner) {
        LiteralToken.Integer first = attemptBase16Int(scanner);
        if (first != null) return first;
        first = attemptBase10Int(scanner);
        if (first == null) return null;
        if (scanner.has("f")) {
            scanner.next();
            scanner.requirePunct();
            return new LiteralToken.Decimal(first.span(), first.value);
        } else if (scanner.has(".")) {
            scanner.next();
            LiteralToken.Integer second = attemptBase10Int(scanner);
            if (second == null)
                return new LiteralToken.Decimal(Span.concat(first.span(), scanner.latestSpan()), first.value);
            else {
                return new LiteralToken.Decimal(Span.concat(first.span(), second.span()), Double.parseDouble("%s.%s".formatted(first.value, second.value)));
            }
        } else {
            return first;
        }
    }

    private static LiteralToken.String attemptString(Scanner scanner) {
        if (scanner.has("\"") || scanner.has("'")) {
            Span.Loc start = scanner.loc();
            char quotes = scanner.next();
            StringBuilder str = new StringBuilder();
            while (!scanner.has("" + quotes)) {
                if(scanner.has("\\\"")) {
                    scanner.next(2);
                    str.append("\\\"");
                } else {
                    str.append(scanner.next());
                }
//                if (scanner.has("\"")) {
//                    scanner.next(2);
//                    str.append("\"");
//                } else if (scanner.has("'")) {
//                    scanner.next(2);
//                    str.append("'");
//                } else if (scanner.has("\\")) {
//                    scanner.next(2);
//                    str.append("\\");
//                } else if (scanner.has("\n")) {
//                    scanner.next(2);
//                    str.append("\n");
//                } else if (scanner.has("\t")) {
//                    scanner.next(2);
//                    str.append("\t");
//                } else {
//                    str.append(scanner.next());
//                }
            }
            if (scanner.has("" + quotes)) {
                scanner.next();
            } else {
                throw new SourceCodeException(scanner.latestSpan(), "expected closing quotes");
            }
            return new LiteralToken.String(scanner.from(start), str.toString());
        }
        return null;
    }

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    public static final String IDENT_START = ALPHABET + ALPHABET.toUpperCase() + "#_!";
    public static final String IDENT_MID = IDENT_START + "0123456789";

    private static TokenTree attemptIdent(Scanner scanner) {
        if (IDENT_START.indexOf(scanner.peek()) == -1) return null;
        Span.Loc start = scanner.loc();
        StringBuilder str = new StringBuilder(scanner.next(1));
        while (IDENT_MID.indexOf(scanner.peek()) != -1) {
            str.append(scanner.next());
        }
        Span span = scanner.from(start);
//        if (str.toString().equals("true")) return new LiteralToken.Boolean(span, true);
//        if (str.toString().equals("false")) return new LiteralToken.Boolean(span, false);
        return new IdentToken(span, str.toString());
    }

    private static List<TokenTree> tokenize(Scanner scanner) {
        List<TokenTree> tokens = new ArrayList<>();
        scanner.purge();
        while (scanner.remaining() > 0) {
            GroupToken group = attemptGroup(scanner);
            if (group != null) {
                tokens.add(group);
                scanner.purge();
                continue;
            }
            PunctToken punct = attemptPunct(scanner);
            if (punct != null) {
                tokens.add(punct);
                scanner.purge();
                continue;
            }
            LiteralToken number = attemptNumber(scanner);
            if (number != null) {
                tokens.add(number);
                scanner.purge();
                continue;
            }
            LiteralToken string = attemptString(scanner);
            if (string != null) {
                tokens.add(string);
                scanner.purge();
                continue;
            }
            TokenTree ident = attemptIdent(scanner);
            if (ident != null) {
                tokens.add(ident);
                scanner.purge();
                continue;
            }
            break;
        }
        scanner.purge();
//        if(scanner.remaining() > 0) throw new SourceCodeException(scanner.latestSpan(), "expected eof");
        return tokens;
    }

    public static List<TokenTree> tokenize(Path file, String data) {
        Scanner scanner = new Scanner(file, data);
        List<TokenTree> tokens = tokenize(scanner);
        if (scanner.remaining() > 0) throw new SourceCodeException(scanner.latestSpan(), "expected eof");
        return tokens;
    }

    private static class Scanner {
        private int lineIndex;
        private int line;
        private int overallIndex;
        private final Path file;
        private final String data;

        public Scanner(Path file, String data) {
            this.file = file;
            this.data = data;

            lineIndex = 0;
            line = 1;
            overallIndex = 0;
        }

        public void requirePunct() {
            if (remaining() > 0) {
                if (!Character.isWhitespace(peek()) && PunctToken.ALL_PUNCT.indexOf(peek()) == -1) {
                    throw new SourceCodeException(latestSpan(), "unexpected char");
                }
            }
        }

        private void purgeIteration() {
            if (has("//")) {
                next(2);
                while (peek() != '\n') {
                    next();
                }
            }
            if (has("/*")) {
                next(2);
                while (!has("*/")) {
                    next();
                }
                next(2);
            }
            while (remaining() > 0 && Character.isWhitespace(peek())) {
                next();
            }
        }

        public void purge() {
            int i = overallIndex;
            while (true) {
                purgeIteration();
                if (overallIndex == i) {
                    break;
                }
                i = overallIndex;
            }
        }

        public Span.Loc loc() {
            return new Span.Loc(lineIndex, line);
        }

        public Span from(Span.Loc loc) {
            return new Span(loc, loc(), file);
        }

        public int remaining() {
            return data.length() - overallIndex;
        }

        public char peek() {
            if (remaining() <= 0)
                throw new SourceCodeException(latestSpan(), "unexpected eof");
            return data.charAt(overallIndex);
        }

        private Span latestSpan() {
            return new Span(loc(), new Span.Loc(lineIndex + 1, line), file);
        }

        public boolean has(String str) {
            return data.startsWith(str, overallIndex);
        }

        public String next(int n) {
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < n; i++) s.append(next());
            return s.toString();
        }

        public char next() {
            char c = peek();
            overallIndex++;
            if (c == '\n') {
                line++;
                lineIndex = 0;
            } else {
                lineIndex++;
            }
            return c;
        }

        public boolean nextIsIn(String str) {
            return str.indexOf(peek()) != -1;
        }
    }

}
