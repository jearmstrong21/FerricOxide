package mackycheese21.ferricoxide.parser;

import mackycheese21.ferricoxide.SourceCodeException;
import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.Pair;
import mackycheese21.ferricoxide.ast.expr.*;
import mackycheese21.ferricoxide.ast.expr.unresolved.*;
import mackycheese21.ferricoxide.ast.type.FOType;
import mackycheese21.ferricoxide.parser.token.*;

import java.util.ArrayList;
import java.util.List;

public class ExpressionParser {

    private static Expression attemptIf(TokenScanner scanner) {
        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("if")) {
            Span start = scanner.next().span();

            TokenScanner condScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.PAREN).value);
            Expression condition = forceExpr(condScanner);
            condScanner.requireEmpty();
            TokenScanner thenScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.CURLY_BRACKET).value);
            Expression then = forceExpr(thenScanner);
            thenScanner.requireEmpty();
            scanner.next().requireIdent().requireValue("else");
            TokenScanner otherwiseScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.CURLY_BRACKET).value);
            Expression otherwise = forceExpr(otherwiseScanner);
            otherwiseScanner.requireEmpty();

            Span end = scanner.lastConsumedSpan();
            return new IfExpr(Span.concat(start, end), condition, then, otherwise);
        }
        return null;
    }

    private static Expression attemptLiteral(TokenScanner scanner) {
        if (scanner.peek() instanceof LiteralToken.String string) {
//            System.out.println("parse string " + string.value);
            return new StringConstant(scanner.next().span(), StringConstant.unescape(string.value));
        }
        if (scanner.peek() instanceof LiteralToken.Integer integer) {
            return new UnresolvedIntConstant(scanner.next().span(), integer.value);
        }
        if (scanner.peek() instanceof LiteralToken.Decimal decimal) {
            return new UnresolvedFloatConstant(scanner.next().span(), decimal.value);
        }
        if (scanner.peek() instanceof LiteralToken.Boolean bool) {
            return new BoolConstant(scanner.next().span(), bool.value);
        }
        return null;
    }

    private static Expression attemptParen(TokenScanner scanner) {
        if (scanner.peek() instanceof GroupToken group && group.type == GroupToken.Type.PAREN) {
            scanner.next();
            TokenScanner exprScanner = new TokenScanner(group.value);
            Expression expr = forceExpr(exprScanner);
            expr.span = group.span();
            exprScanner.requireEmpty();
            return expr;
        }
        return null;
    }

    private static Expression attemptUnary(TokenScanner scanner) {
        if (scanner.peek() instanceof PunctToken punct) {
            for (UnaryOperator operator : UnaryOperator.values()) {
                if (operator.punctuation == punct.type) {
                    Span start = scanner.next().span();
                    Expression expr = forceExpr(scanner);
                    return new Unary(Span.concat(start, expr.span), expr, operator);
                }
            }
        }
        return null;
    }

    private static Expression attemptAccessVar(TokenScanner scanner) {
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
        return new UnresolvedAccessVar(Span.concat(start, identifier.span), ref, identifier);
    }

    private static Expression attemptSizeof(TokenScanner scanner) {
        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("sizeof")) {
            Span start = scanner.next().span();
            TokenScanner typeScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.PAREN).value);
            FOType type = CommonParser.forceType(typeScanner);
            typeScanner.requireEmpty();
            Span end = scanner.lastConsumedSpan();
            return new SizeOf(Span.concat(start, end), type);
        }
        return null;
    }

    private static Expression attemptStructInit(TokenScanner scanner) {
        TokenScanner s = scanner.copy();
        Identifier struct = CommonParser.attemptIdentifier(s);
        if (struct == null) return null;
        Span start = struct.span;
        if (s.remaining() > 0 && s.peek() instanceof GroupToken group && group.type == GroupToken.Type.CURLY_BRACKET) {
            s.next();
            scanner.from(s);
            TokenScanner initializerScanner = new TokenScanner(group.value);
            List<Pair<String, Expression>> fields = new ArrayList<>();
            while (initializerScanner.remaining() > 0) {
                if (fields.size() > 0) {
                    initializerScanner.next().requirePunct(PunctToken.Type.COMMA);
                }
                String str = initializerScanner.next().requireIdent().value;
                initializerScanner.next().requirePunct(PunctToken.Type.COLON);
                Expression expr = forceExpr(initializerScanner);
                fields.add(new Pair<>(str, expr));
            }
            initializerScanner.requireEmpty();
            Span end = scanner.lastConsumedSpan();
            return new UnresolvedStructInit(Span.concat(start, end), struct, fields);
        }
        return null;
    }

    private static Expression attemptZeroinit(TokenScanner scanner) {
        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("zeroinit")) {
            Span start = scanner.next().span();
            TokenScanner typeScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.PAREN).value);
            FOType type = CommonParser.forceType(typeScanner);
            typeScanner.requireEmpty();
            Span end = scanner.lastConsumedSpan();
            return new ZeroInit(Span.concat(start, end), type);
        }
        return null;
    }

    private static Expression forceSimpleFirst(TokenScanner scanner) {
        Expression expr;
        if ((expr = attemptParen(scanner)) != null) return expr;
        if ((expr = attemptLiteral(scanner)) != null) return expr;
        if ((expr = attemptZeroinit(scanner)) != null) return expr;
        if ((expr = attemptSizeof(scanner)) != null) return expr;
        if ((expr = attemptIf(scanner)) != null) return expr;
        if ((expr = attemptUnary(scanner)) != null) return expr;
        if ((expr = attemptStructInit(scanner)) != null) return expr;
        if ((expr = attemptAccessVar(scanner)) != null) return expr;
        throw new SourceCodeException(scanner.peek().span(), "expected simple first");
    }

    private static Expression forceSimple(TokenScanner scanner) {
        Span span = null;
        boolean ref = scanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.AND;
        if (ref) {
            span = scanner.next().span();
        }
        Expression simple = forceSimpleFirst(scanner);
        span = Span.concat(span, simple.span);
        while (scanner.remaining() > 0) {
            if (scanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.DOT) {
                scanner.next();
                FOType.Access access = null;
                if (scanner.peek() instanceof IdentToken ident)
                    access = FOType.Access.string(scanner.next().requireIdent().value);
                else if (scanner.peek() instanceof LiteralToken.Integer integer) {
                    scanner.next();
                    access = FOType.Access.integer((int) integer.value);
                }
                if (access == null) throw new SourceCodeException(scanner.lastConsumedSpan(), "expected literal/int");
                span = Span.concat(span, scanner.lastConsumedSpan());
                simple = new UnresolvedAccessProperty(span, simple, access, false, false);
            } else if (scanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.R_ARROW) {
                scanner.next();
                FOType.Access access = FOType.Access.string(scanner.next().requireIdent().value);
                span = Span.concat(span, scanner.lastConsumedSpan());
                simple = new UnresolvedAccessProperty(span, simple, access, true, false);
            } else if (scanner.peek() instanceof GroupToken group && group.type == GroupToken.Type.BRACKET) {
                span = Span.concat(span, scanner.next().span());
                TokenScanner indexScanner = new TokenScanner(group.value);
                Expression index = forceExpr(indexScanner);
                indexScanner.requireEmpty();
                simple = new ArrayIndex(span, simple, index, false);
            } else if (scanner.peek() instanceof GroupToken group && group.type == GroupToken.Type.PAREN) {
                span = Span.concat(span, scanner.next().span());
                TokenScanner paramScanner = new TokenScanner(group.value);
                List<Expression> params = new ArrayList<>();
                while (paramScanner.remaining() > 0) {
                    if (params.size() > 0) paramScanner.next().requirePunct(PunctToken.Type.COMMA);
                    params.add(forceExpr(paramScanner));
                }
                paramScanner.requireEmpty();
                simple = new CallExpr(span, simple, params);
            } else if (scanner.peek() instanceof IdentToken ident && ident.value.equals("as")) {
                scanner.next();
                FOType type = CommonParser.forceType(scanner);
                span = Span.concat(span, scanner.lastConsumedSpan());
                simple = new CastExpr(span, type, simple);
            } else {
                break;
            }
        }
        if (ref) {
            if (simple instanceof UnresolvedAccessProperty uap) uap.explicitRef = true;
            else if (simple instanceof UnresolvedAccessVar uav) uav.explicitRef = true;
            else if (simple instanceof ArrayIndex ai) ai.ref = true;
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

    private static Expression binaryExpr(TokenScanner scanner, int minPriority) {
        Expression result = forceSimple(scanner);
        BinaryOperator operator = binaryOperatorPeek(scanner);
        while (operator != null && operator.priority >= minPriority) {
            scanner.next();
            Expression rhs = binaryExpr(scanner, operator.priority + 1);
            result = new Binary(Span.concat(result.span, rhs.span), result, rhs, operator);
            operator = binaryOperatorPeek(scanner);
        }
        return result;
    }

    public static Expression forceExpr(TokenScanner scanner) {
        return binaryExpr(scanner, 0);
    }

    // https://eli.thegreenplace.net/2012/08/02/parsing-expressions-by-precedence-climbing

}
