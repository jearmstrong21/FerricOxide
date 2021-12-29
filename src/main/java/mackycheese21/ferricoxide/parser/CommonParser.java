package mackycheese21.ferricoxide.parser;

import mackycheese21.ferricoxide.SourceCodeException;
import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.type.*;
import mackycheese21.ferricoxide.parser.token.Span;
import mackycheese21.ferricoxide.parser.token.Token;
import mackycheese21.ferricoxide.parser.token.TokenScanner;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CommonParser {

    public static List<String> MODULE_PATH = new ArrayList<>();

    @FunctionalInterface
    public interface Parse<T> {
        /**
         * value = success
         * null = next-alternative failure
         * throw = propogate failure
         * It is not legal to throw AlternativeSkipException, although intermediates may be run through
         * AlternativeSkipException#process to become valid Parses
         */
        @Nullable
        T parse(TokenScanner scanner);
    }

    @SafeVarargs
    public static <T> Parse<T> alt(String error, Parse<T>... parses) {
        return scanner -> {
            for (Parse<T> parse : parses) {
                T result = parse.parse(scanner);
                if (result != null) return result;
            }
            if(error == null) throw new AlternativeSkipException();
            throw new SourceCodeException(error, scanner.currentOrLast().span);
        };
    }

    public static final Parse<String> ID_TOKEN = attempt(scanner -> scanner.hasNext(Token.Type.IDENTIFIER) ? scanner.next().identifier() : null);
    public static final Parse<Token> COMMA = attempt(scanner -> scanner.hasNext(Token.Punctuation.COMMA) ? scanner.next() : null);
    public static final Parse<Token> DOUBLE_COLON = attempt(scanner -> scanner.hasNext(Token.Punctuation.DOUBLE_COLON) ? scanner.next() : null);

    public static <T> Parse<List<T>> commaSeparatedList(Parse<T> element, Parse<Token> separator) {
        return scanner -> {
            List<T> list = new ArrayList<>();
            while (true) {
                if (list.size() > 0) {
                    if (separator.parse(scanner) == null) break;
                }
                try {
                    T result = element.parse(scanner);
                    if (result == null) break;
                    list.add(result);
                } catch (SourceCodeException | AlternativeSkipException ignored) {
                    break;
                }
            }
            return list;
        };
    }

    public static <T> Parse<T> attempt(Parse<T> parse) {
        return scanner -> {
            TokenScanner s = scanner.copy();
            T result = parse.parse(s);
            if (result == null) return null;
            scanner.index = s.index;
            return result;
        };
    }

    public static Parse<Identifier> IDENTIFIER = attempt(scanner -> {
        Span start = scanner.spanAtRel(0);
        boolean global = scanner.hasNext(Token.Punctuation.DOUBLE_COLON);
        if(global) scanner.next();
        List<String> strings = commaSeparatedList(ID_TOKEN, DOUBLE_COLON).parse(scanner);
        if (strings == null || strings.size() == 0) return null;
        Span end = scanner.spanAtRel(-1);
        return new Identifier(Span.concat(start, end), strings);
    });

    private static final Parse<FOType> KEYWORD_TYPE = attempt(scanner -> {
        Token next = scanner.next();
        if (next.is(Token.Keyword.BOOL)) return FOType.BOOL;
        if (next.is(Token.Keyword.I8)) return FOType.I8;
        if (next.is(Token.Keyword.I16)) return FOType.I16;
        if (next.is(Token.Keyword.I32)) return FOType.I32;
        if (next.is(Token.Keyword.I64)) return FOType.I64;
        if (next.is(Token.Keyword.F32)) return FOType.F32;
        if (next.is(Token.Keyword.F64)) return FOType.F64;
        return null;
    });

    private static final Parse<FOType> UNRESOLVED_TYPE = attempt(scanner -> {
        Identifier identifier = IDENTIFIER.parse(scanner);
        if (identifier != null) return new UnresolvedType(identifier);
        return null;
    });

    private static final Parse<FOType> TUPLE_TYPE = AlternativeSkipException.process(scanner -> {
        scanner.next().skipIfNot(Token.Punctuation.L_PAREN);
        List<FOType> types = commaSeparatedList(CommonParser.TYPE, COMMA).parse(scanner);
        scanner.next().mustBe(Token.Punctuation.R_PAREN);
        return new TupleType(types);
    });

    private static final Parse<FOType> FUNCTION_TYPE = attempt(scanner -> {
        scanner.next().skipIfNot(Token.Keyword.FN);
        if(!scanner.next().is(Token.Punctuation.LT)) return null;
        List<FOType> params = commaSeparatedList(CommonParser.TYPE, COMMA).parse(scanner);
        scanner.next().mustBe(Token.Punctuation.GT);
        scanner.next().mustBe(Token.Punctuation.L_PAREN);
        FOType result = CommonParser.TYPE.parse(scanner);
        scanner.next().mustBe(Token.Punctuation.R_PAREN);
        return new FunctionType(result, params);
    });

    private static final Parse<FOType> SIMPLE_TYPE = alt(null, KEYWORD_TYPE, UNRESOLVED_TYPE, TUPLE_TYPE, FUNCTION_TYPE);

    public static Parse<FOType> TYPE = AlternativeSkipException.process(scanner -> {
        FOType simple = SIMPLE_TYPE.parse(scanner);
        if(simple == null) return null;
        while (scanner.hasNext(Token.Punctuation.STAR)) {
            simple = new PointerType(simple);
            scanner.next();
        }
        return simple;
    });

}
