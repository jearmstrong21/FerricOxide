package mackycheese21.ferricoxide.parser;

import mackycheese21.ferricoxide.Pair;
import mackycheese21.ferricoxide.SourceCodeException;
import mackycheese21.ferricoxide.ast.BinaryOperator;
import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.UnaryOperator;
import mackycheese21.ferricoxide.ast.hl.expr.*;
import mackycheese21.ferricoxide.ast.hl.type.HLType;
import mackycheese21.ferricoxide.parser.token.*;

import java.util.ArrayList;
import java.util.List;

public class ExpressionParser {

    private static HLExpression attemptIf(TokenScanner scanner) {
        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("if")) {
            Span start = scanner.next().span();

            TokenScanner condScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.PAREN).value);
            HLExpression condition = forceExpr(condScanner);
            condScanner.requireEmpty();
            TokenScanner thenScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.CURLY_BRACKET).value);
            HLExpression then = forceExpr(thenScanner);
            thenScanner.requireEmpty();
            scanner.next().requireIdent().requireValue("else");
            TokenScanner otherwiseScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.CURLY_BRACKET).value);
            HLExpression otherwise = forceExpr(otherwiseScanner);
            otherwiseScanner.requireEmpty();

            Span end = scanner.lastConsumedSpan();
            return new HLIfExpr(Span.concat(start, end), condition, then, otherwise);
        }
        return null;
    }

    private static HLExpression attemptLiteral(TokenScanner scanner) {
        if (scanner.peek() instanceof LiteralToken.String string) {
            return new HLStringConstant(scanner.next().span(), HLStringConstant.unescape(string.value));
        }
        if (scanner.peek() instanceof LiteralToken.Integer integer) {
            return new HLIntConstant(scanner.next().span(), integer.value);
        }
        if (scanner.peek() instanceof LiteralToken.Decimal decimal) {
            return new HLFloatConstant(scanner.next().span(), decimal.value);
        }
        if (scanner.peek() instanceof LiteralToken.Boolean bool) {
            return new HLBoolConstant(scanner.next().span(), bool.value);
        }
        return null;
    }

    private static HLExpression attemptParen(TokenScanner scanner) {
        if (scanner.peek() instanceof GroupToken group && group.type == GroupToken.Type.PAREN) {
            scanner.next();
            TokenScanner exprScanner = new TokenScanner(group.value);
            HLExpression expr = forceExpr(exprScanner);
            exprScanner.requireEmpty();
            return new HLParen(group.span(), expr);
        }
        return null;
    }

    private static HLExpression attemptDeref(TokenScanner scanner) {
        if(scanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.ASTERISK) {
            Span start = scanner.next().span();
            HLExpression expr = forceExpr(scanner);
            return new HLDeref(Span.concat(start, expr.span), expr);
        }
        return null;
    }

    private static HLExpression attemptUnary(TokenScanner scanner) {
        if (scanner.peek() instanceof PunctToken punct) {
            for (UnaryOperator operator : UnaryOperator.values()) {
                if (operator.punctuation == punct.type) {
                    Span start = scanner.next().span();
                    HLExpression expr = forceExpr(scanner);
                    return new HLUnary(Span.concat(start, expr.span), operator, expr);
                }
            }
        }
        return null;
    }

    private static HLExpression attemptAccessVar(TokenScanner scanner) {
        boolean ref = scanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.AND;
        Span start = null;
        if (ref) {
            start = scanner.next().span();
        }
        Identifier identifier = CommonParser.attemptIdentifier(scanner);
        if (identifier == null) {
            if (!ref) return null;
            else throw new SourceCodeException(scanner.lastConsumedSpan(), "expected identifier after ref");
        }
        HLExpression expr = new HLAccess.Var(Span.concat(start, identifier.span), identifier, ref);
        return expr;
    }

    private static HLExpression attemptSizeof(TokenScanner scanner) {
        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("sizeof")) {
            Span start = scanner.next().span();
            TokenScanner typeScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.PAREN).value);
            HLType type = CommonParser.forceType(typeScanner);
            typeScanner.requireEmpty();
            Span end = scanner.lastConsumedSpan();
            return new HLSizeOf(Span.concat(start, end), type);
        }
        return null;
    }

    private static HLExpression attemptStructInit(TokenScanner scanner) {
        TokenScanner s = scanner.copy();
        Identifier struct = CommonParser.attemptIdentifier(s);
        if (struct == null) return null;
        Span start = struct.span;
        if (s.remaining() > 0 && s.peek() instanceof GroupToken group && group.type == GroupToken.Type.CURLY_BRACKET) {
            s.next();
            scanner.from(s);
            TokenScanner initializerScanner = new TokenScanner(group.value);
            List<Pair<String, HLExpression>> fields = new ArrayList<>();
            while (initializerScanner.remaining() > 0) {
                if (fields.size() > 0) {
                    initializerScanner.next().requirePunct(PunctToken.Type.COMMA);
                }
                String str = initializerScanner.next().requireIdent().value;
                initializerScanner.next().requirePunct(PunctToken.Type.COLON);
                HLExpression expr = forceExpr(initializerScanner);
                fields.add(new Pair<>(str, expr));
            }
            initializerScanner.requireEmpty();
            Span end = scanner.lastConsumedSpan();
            return new HLStructInit(Span.concat(start, end), struct, fields);
        }
        return null;
    }

    private static HLExpression attemptZeroinit(TokenScanner scanner) {
        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("zeroinit")) {
            Span start = scanner.next().span();
            TokenScanner typeScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.PAREN).value);
            HLType type = CommonParser.forceType(typeScanner);
            typeScanner.requireEmpty();
            Span end = scanner.lastConsumedSpan();
            return new HLZeroInit(Span.concat(start, end), type);
        }
        return null;
    }

    private static HLExpression forceSimpleFirst(TokenScanner scanner) {
        HLExpression expr;
        if ((expr = attemptParen(scanner)) != null) return expr;
        if ((expr = attemptLiteral(scanner)) != null) return expr;
        if ((expr = attemptZeroinit(scanner)) != null) return expr;
        if ((expr = attemptSizeof(scanner)) != null) return expr;
        if ((expr = attemptIf(scanner)) != null) return expr;
        if ((expr = attemptDeref(scanner)) != null) return expr;
        if ((expr = attemptUnary(scanner)) != null) return expr;
        if ((expr = attemptStructInit(scanner)) != null) return expr;
        if ((expr = attemptAccessVar(scanner)) != null) return expr;
        throw new SourceCodeException(scanner.peek().span(), "expected simple first");
    }

    private static HLExpression forceSimple(TokenScanner scanner) {
        Span span = null;
        boolean ref = scanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.AND;
        if (ref) {
            span = scanner.next().span();
        }
        HLExpression simple = forceSimpleFirst(scanner);
        span = Span.concat(span, simple.span);
        while (scanner.remaining() > 0) {
            if (scanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.DOT) {
                scanner.next();

                TokenTree access = scanner.next();
                span = Span.concat(span, access.span());
                if (access instanceof IdentToken ident) {
                    simple = new HLAccess.Property.Name(span, simple, ident.value, false);
                } else if (access instanceof LiteralToken.Integer integer) {
                    simple = new HLAccess.Property.Index(span, simple, (int) integer.value, false);
                } else {
                    throw new SourceCodeException(access.span(), "expected literal/int in dot access");
                }

            } else if (scanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.R_ARROW) {
                scanner.next();

                span = Span.concat(span, punct.span());
                simple = new HLDeref(span, simple);

                TokenTree access = scanner.next();
                span = Span.concat(span, access.span());
                if (access instanceof IdentToken ident) {
                    simple = new HLAccess.Property.Name(span, simple, ident.value, false);
                } else if (access instanceof LiteralToken.Integer integer) {
                    simple = new HLAccess.Property.Index(span, simple, (int) integer.value, false);
                } else {
                    throw new SourceCodeException(access.span(), "expected literal/int in arrow access");
                }

            } else if (scanner.peek() instanceof GroupToken group && group.type == GroupToken.Type.BRACKET) {
                span = Span.concat(span, scanner.next().span());
                TokenScanner indexScanner = new TokenScanner(group.value);
                HLExpression index = forceExpr(indexScanner);
                indexScanner.requireEmpty();
                simple = new HLAccess.Index(span, simple, index, false);
            } else if (scanner.peek() instanceof GroupToken group && group.type == GroupToken.Type.PAREN) {
                span = Span.concat(span, scanner.next().span());
                TokenScanner paramScanner = new TokenScanner(group.value);
                List<HLExpression> params = new ArrayList<>();
                while (paramScanner.remaining() > 0) {
                    if (params.size() > 0) paramScanner.next().requirePunct(PunctToken.Type.COMMA);
                    params.add(forceExpr(paramScanner));
                }
                paramScanner.requireEmpty();
                simple = new HLCallExpr(span, simple, params);
            } else if (scanner.peek() instanceof IdentToken ident && ident.value.equals("as")) {
                scanner.next();
                HLType type = CommonParser.forceType(scanner);
                span = Span.concat(span, scanner.lastConsumedSpan());
                simple = new HLCast(span, simple, type);
            } else {
                break;
            }
        }
        if (ref) {
            if (simple instanceof HLAccess.Var var) var.ref = true;
            else if (simple instanceof HLAccess.Index index) index.ref = true;
            else if (simple instanceof HLAccess.Property.Name name) name.ref = true;
            else if (simple instanceof HLAccess.Property.Index index) index.ref = true;
            else throw new SourceCodeException(span, "cannot take reference");
        }
        return simple;
    }

    private static BinaryOperator binaryOperatorPeek(TokenScanner scanner) {
        if (scanner.remaining() > 0 && scanner.peek() instanceof PunctToken punct) {
            for (BinaryOperator operator : BinaryOperator.values()) {
                if (operator.punctType == punct.type) {
                    return operator;
                }
            }
        }
        return null;
    }

    private static HLExpression binaryExpr(TokenScanner scanner, int minPriority) {
        HLExpression result = forceSimple(scanner);
        BinaryOperator operator = binaryOperatorPeek(scanner);
        while (operator != null && operator.priority >= minPriority) {
            scanner.next();
            HLExpression rhs = binaryExpr(scanner, operator.priority + 1);
            result = new HLBinary(Span.concat(result.span, rhs.span), result, operator, rhs);
            operator = binaryOperatorPeek(scanner);
        }
        return result;
    }

    public static HLExpression forceExpr(TokenScanner scanner) {
        return binaryExpr(scanner, 0);
    }

    // https://eli.thegreenplace.net/2012/08/02/parsing-expressions-by-precedence-climbing

}
