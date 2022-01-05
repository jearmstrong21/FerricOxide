package mackycheese21.ferricoxide.parser;

import mackycheese21.ferricoxide.SourceCodeException;
import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.type.*;
import mackycheese21.ferricoxide.parser.token.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommonParser {

    public static @Nullable Identifier attemptIdentifier(TokenScanner scanner) {
        if (scanner.peek() instanceof IdentToken ident) {
            Span span = ident.span();
            List<String> strs = new ArrayList<>();

            scanner.next();
            strs.add(ident.value);

            while (scanner.remaining() > 0 && scanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.COLONCOLON) {
                scanner.next();
                IdentToken nextIdent = scanner.next().requireIdent();
                strs.add(nextIdent.value);
                span = Span.concat(span, nextIdent.span());
            }
            return new Identifier(span, strs);
        }
        return null;
    }

    public static @NotNull Identifier forceIdentifier(TokenScanner scanner) {
        Identifier result = attemptIdentifier(scanner);
        if (result == null) throw new SourceCodeException(scanner.peek().span(), "expected identifier");
        return result;
    }

    private static final Map<String, FOType> KEYWORD_TYPES = Map.ofEntries(
            Map.entry("bool", FOType.BOOL),
            Map.entry("i8", FOType.I8),
            Map.entry("i16", FOType.I16),
            Map.entry("i32", FOType.I32),
            Map.entry("i64", FOType.I64),
            Map.entry("f32", FOType.F32),
            Map.entry("f64", FOType.F64),
            Map.entry("void", FOType.VOID)
    );

    private static @Nullable FOType attemptKeywordType(TokenScanner scanner) {
        if (scanner.peek() instanceof IdentToken ident && KEYWORD_TYPES.containsKey(ident.value)) {
            scanner.next();
            return KEYWORD_TYPES.get(ident.value);
        }
        return null;
    }

    private static @Nullable FOType attemptTupleType(TokenScanner scanner) {
        if (scanner.peek() instanceof GroupToken group && group.type == GroupToken.Type.PAREN) {
            scanner.next();
            TokenScanner groupScanner = new TokenScanner(group.value);
            List<FOType> types = new ArrayList<>();
            while (groupScanner.remaining() > 0) {
                if (types.size() > 0) {
                    groupScanner.next().requirePunct(PunctToken.Type.COMMA);
                }
                types.add(forceType(groupScanner));
            }
            return new TupleType(types);
        }
        return null;
    }

    private static @Nullable FOType attemptFunctionType(TokenScanner scanner) {
        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("fn")) {
            scanner.next();
            TokenScanner paramScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.PAREN).value);
            List<FOType> params = new ArrayList<>();
            while (paramScanner.remaining() > 0) {
                if (params.size() > 0) {
                    paramScanner.next().requirePunct(PunctToken.Type.COMMA);
                }
                params.add(forceType(paramScanner));
            }
            paramScanner.requireEmpty();
            FOType result;
            if (scanner.next() instanceof PunctToken punct && punct.type == PunctToken.Type.R_ARROW) {
                scanner.next().requirePunct(PunctToken.Type.R_ARROW);
                result = forceType(scanner);
            } else {
                result = FOType.VOID;
            }
            return new FunctionType(result, params);
        }
        return null;
    }

    public static @Nullable FOType attemptType(TokenScanner scanner) {
        FOType simple;
        simple = attemptKeywordType(scanner);
        if (simple == null) {
            simple = attemptTupleType(scanner);
        }
        if (simple == null) {
            simple = attemptFunctionType(scanner);
        }
        if (simple == null) {
            Identifier identifier = attemptIdentifier(scanner);
            if (identifier != null) simple = new UnresolvedType(identifier);
        }
        if (simple == null) return null;
        while (scanner.remaining() > 0 && scanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.ASTERISK) {
            scanner.next();
            simple = new PointerType(simple);
        }
        return simple;
    }

    public static @NotNull FOType forceType(TokenScanner scanner) {
        FOType type = attemptType(scanner);
        if (type == null) throw new SourceCodeException(scanner.peek().span(), "expected type");
        return type;
    }

}
