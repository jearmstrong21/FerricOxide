package mackycheese21.ferricoxide.parser;

import mackycheese21.ferricoxide.Identifier;
import mackycheese21.ferricoxide.SourceCodeException;
import mackycheese21.ferricoxide.nast.hl.type.*;
import mackycheese21.ferricoxide.parser.token.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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

//    private static final Map<String, HLKeywordType.Type> KEYWORD_TYPES = Map.ofEntries(
//            Map.entry("bool", HLKeywordType.Type.BOOL),
//            Map.entry("i8", HLKeywordType.Type.I8),
//            Map.entry("i16", HLKeywordType.Type.I16),
//            Map.entry("i32", HLKeywordType.Type.I32),
//            Map.entry("i64", HLKeywordType.Type.I64),
//            Map.entry("f32", HLKeywordType.Type.F32),
//            Map.entry("f64", HLKeywordType.Type.F64),
//            Map.entry("void", HLKeywordType.Type.VOID)
//    );

    private static @Nullable HLTypeId attemptKeywordType(TokenScanner scanner) {
//        if (scanner.peek() instanceof IdentToken ident && KEYWORD_TYPES.containsKey(ident.value)) {
//            scanner.next();
//            return new HLKeywordType(scanner.lastConsumedSpan(), KEYWORD_TYPES.get(ident.value));
//        }
        if (scanner.peek() instanceof IdentToken ident) {
            Span span = ident.span();
            switch (ident.value) {
//                case "void" -> HLTypeId.none()
                case "i32" -> {
                    scanner.next();
                    return HLTypeId.i32(span);
                }
                case "u32" -> {
                    scanner.next();
                    return HLTypeId.u32(span);
                }
                case "f32" -> {
                    scanner.next();
                    return HLTypeId.f32(span);
                }
            }
        }
        return null;
    }

    private static @Nullable HLTypeId attemptIdentifierType(TokenScanner scanner) {
        Identifier identifier = attemptIdentifier(scanner);
        if (identifier != null) return new HLIdentifierTypeId(identifier.span, identifier);
        return null;
    }

    private static @Nullable HLTypeId attemptTupleType(TokenScanner scanner) {
        if (scanner.peek() instanceof GroupToken group && group.type == GroupToken.Type.PAREN) {
            scanner.next();
            TokenScanner groupScanner = new TokenScanner(group.value);
            List<HLTypeId> types = new ArrayList<>();
            while (groupScanner.remaining() > 0) {
                if (types.size() > 0) {
                    groupScanner.next().requirePunct(PunctToken.Type.COMMA);
                }
                types.add(forceType(groupScanner));
            }
            if (types.size() == 0) return HLTypeId.none(group.span());
            return new HLTupleTypeId(group.span(), types);
        }
        return null;
    }

    private static @Nullable HLTypeId attemptFunctionType(TokenScanner scanner) {
        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("fn")) {
            scanner.next();
            Span start = scanner.lastConsumedSpan();
            TokenScanner paramScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.PAREN).value);
            List<HLTypeId> params = new ArrayList<>();
            while (paramScanner.remaining() > 0) {
                if (params.size() > 0) {
                    paramScanner.next().requirePunct(PunctToken.Type.COMMA);
                }
                params.add(forceType(paramScanner));
            }
            paramScanner.requireEmpty();
            HLTypeId result;
            if (scanner.next() instanceof PunctToken punct && punct.type == PunctToken.Type.R_ARROW) {
                scanner.next().requirePunct(PunctToken.Type.R_ARROW);
                result = forceType(scanner);
            } else {
                result = HLTypeId.none(ident.span());
            }
            Span end = scanner.lastConsumedSpan();
            return new HLFunctionTypeId(Span.concat(start, end), params, result);
        }
        return null;
    }

    public static @Nullable HLTypeId attemptType(TokenScanner scanner) {
        HLTypeId simple;
        List<Span> starSpans = new ArrayList<>();
        Span span = Span.NONE;
        while (scanner.remaining() > 0 && scanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.ASTERISK) {
            span = Span.concat(span, scanner.next().span());
            starSpans.add(scanner.lastConsumedSpan());
        }
        simple = attemptKeywordType(scanner);
        if (simple == null)
            simple = attemptIdentifierType(scanner);
        if (simple == null)
            simple = attemptFunctionType(scanner);
        if (simple == null) {
            if (starSpans.size() == 0) return null;
            throw new SourceCodeException(span, "unexpected asterisks");
        }
        for (int i = starSpans.size() - 1; i >= 0; i++) {
            simple = new HLPointerTypeId(span, simple);
        }
        return simple;
    }

    public static @NotNull HLTypeId forceType(TokenScanner scanner) {
        HLTypeId type = attemptType(scanner);
        if (type == null) throw new SourceCodeException(scanner.peek().span(), "expected type");
        return type;
    }

}
