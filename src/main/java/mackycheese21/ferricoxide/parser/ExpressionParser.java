package mackycheese21.ferricoxide.parser;

import mackycheese21.ferricoxide.*;
import mackycheese21.ferricoxide.nast.hl.expr.*;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;
import mackycheese21.ferricoxide.parser.token.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ExpressionParser {

    private static HLExpression attemptIf(TokenScanner scanner) {
        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("if")) {
            Span start = scanner.next().span();

            HLExpression condition = forceExpr(scanner);
            HLExpression then = forceBlock(scanner);
            HLExpression otherwise = HLExpression.none(ident.span());
            if(scanner.remaining() > 0 && scanner.peek() instanceof IdentToken ident2 && ident2.value.equals("else")) {
                scanner.next();
                otherwise = forceBlock(scanner);
            }

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
        return null;
    }

    private static HLExpression attemptParen(TokenScanner scanner) {
        if (scanner.peek() instanceof GroupToken group && group.type == GroupToken.Type.PAREN) {
            scanner.next();
            TokenScanner exprScanner = new TokenScanner(group.value);
            HLExpression expr = forceExpr(exprScanner);
            exprScanner.requireEmpty();
            return expr; // TODO for formatting purposes return a HLFormatParen node or something
        }
        return null;
    }

    private static HLExpression attemptUnary(TokenScanner scanner) {
        if (scanner.peek() instanceof PunctToken punct) {
            if (punct.type == PunctToken.Type.AND) {
                Span start = scanner.next().span();
                HLExpression expr = forceExpr(scanner);
                return new HLCreateRef(Span.concat(start, expr.span), expr);
            }
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
        Identifier identifier = CommonParser.attemptIdentifier(scanner);
        if (identifier == null) return null;
        return new HLAccessIdentifier(identifier.span, identifier);
    }

    private static HLExpression attemptSizeof(TokenScanner scanner) {
        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("sizeof")) {
            Span start = scanner.next().span();
            TokenScanner typeScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.PAREN).value);
            HLTypeId type = CommonParser.forceType(typeScanner);
            typeScanner.requireEmpty();
            Span end = scanner.lastConsumedSpan();
            return new HLSizeOf(Span.concat(start, end), type);
        }
        return null;
    }

    private static HLExpression attemptStructInit(TokenScanner scanner) {
        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("new")) {
            Span start = scanner.next().span();
            HLTypeId struct = CommonParser.forceType(scanner);
            TokenScanner initializerScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.CURLY_BRACKET).value);
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
            HLTypeId type = CommonParser.forceType(typeScanner);
            typeScanner.requireEmpty();
            Span end = scanner.lastConsumedSpan();
            return new HLZeroInit(Span.concat(start, end), type);
        }
        return null;
    }

    private static HLBlock attemptBlock(TokenScanner scanner) {
        if (scanner.peek() instanceof GroupToken group && group.type == GroupToken.Type.CURLY_BRACKET) {
            scanner.next();
            TokenScanner blockScanner = new TokenScanner(group.value);
            List<HLExpression> exprs = new ArrayList<>();
            while (blockScanner.remaining() > 0) {
                if (exprs.size() > 0) {
                    blockScanner.next().requirePunct(PunctToken.Type.SEMICOLON);
                }
                if (blockScanner.remaining() == 0) break;
                HLExpression expr = forceExpr(blockScanner);
                exprs.add(expr);
            }
            blockScanner.requireEmpty();
            return new HLBlock(scanner.lastConsumedSpan(), exprs);
        }
        return null;
    }

    public static @NotNull HLBlock forceBlock(TokenScanner scanner) {
        HLBlock block = attemptBlock(scanner);
        if (block == null) throw new SourceCodeException(scanner.peek().span(), "expected block");
        return block;
    }

    private static HLExpression attemptReturn(TokenScanner scanner) {
        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("return")) {
            Span start = scanner.next().span();
            if (scanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.SEMICOLON) {
                Span end = scanner.next().span();
                return new HLReturn(Span.concat(start, end), HLExpression.none(Span.concat(start, end)));//TODO wrap in discard? no..
            }
            HLExpression expr = ExpressionParser.forceExpr(scanner);
            return new HLReturn(Span.concat(start, scanner.lastConsumedSpan()), expr);
        }
        return null;
    }

//    private static HLExpression attemptWhile(TokenScanner scanner) {
//        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("while")) {
//            Span start = scanner.next().span();
//            HLExpression cond = ExpressionParser.forceExpr(scanner);
//            HLBlock body = forceBlock(scanner);
//            Span end = scanner.lastConsumedSpan();
//            throw new UnsupportedOperationException("while? screw you");
//            return new HLWhile(Span.concat(start, end), cond, body.statements);
//        }
//        return null;
//    }

    private static HLExpression attemptDeclareVar(TokenScanner scanner) {
        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("let")) {
            Span start = scanner.next().span();
            String name = scanner.next().requireIdent().value;
            HLTypeId type = null;
            if (scanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.COLON) {
                scanner.next();
                type = CommonParser.forceType(scanner);
            }
            if (type == null) throw new UnsupportedOperationException("type inference? screw you");
            scanner.next().requirePunct(PunctToken.Type.EQ);
            HLExpression value = ExpressionParser.forceExpr(scanner);
            Span end = scanner.lastConsumedSpan();
            return new HLDeclareLocal(Span.concat(start, end), name, type, value);
        }
        return null;
    }

    private static HLExpression attemptLoop(TokenScanner scanner) {
        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("loop")) {
            scanner.next();
            Span start = scanner.lastConsumedSpan();
            HLBlock block = forceBlock(scanner);
            return new HLLoop(Span.concat(start, block.span), block.exprs);
        }
        return null;
    }

    private static HLExpression attemptBreak(TokenScanner scanner) {
        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("break")) {
            Span start = scanner.next().span();
            Span end = scanner.next().requirePunct(PunctToken.Type.SEMICOLON).span();
            throw new UnsupportedOperationException("break? screw u");
//            return new HLBreak(Span.concat(start, end));
        }
        return null;
    }

//    private static HLExpression attemptFor(TokenScanner scanner) {
//        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("for")) {
//            Span start = scanner.next().span();
//            TokenScanner forScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.PAREN).value);
//            HLExpression init = forceStatement(forScanner, true);
//            HLExpression condition = ExpressionParser.forceExpr(forScanner);
//            forScanner.next().requirePunct(PunctToken.Type.SEMICOLON);
//            HLExpression update = forceStatement(forScanner, false);
//            forScanner.requireEmpty();
//            HLBlock block = forceBlock(scanner);
//            return new HLFor(Span.concat(start, block.span), init, condition, update, block.statements);
//        }
//        return null;
//    }

    private static HLExpression forceSimpleFirst(TokenScanner scanner) {
        HLExpression expr;
        if ((expr = attemptParen(scanner)) != null) return expr;
        if ((expr = attemptLiteral(scanner)) != null) return expr;
        if ((expr = attemptZeroinit(scanner)) != null) return expr;
        if ((expr = attemptSizeof(scanner)) != null) return expr;
        if ((expr = attemptIf(scanner)) != null) return expr;
        if ((expr = attemptUnary(scanner)) != null) return expr;
        if ((expr = attemptStructInit(scanner)) != null) return expr;
        if ((expr = attemptReturn(scanner)) != null) return expr;
        if ((expr = attemptDeclareVar(scanner)) != null) return expr;
        if ((expr = attemptLoop(scanner)) != null) return expr;
        if ((expr = attemptBreak(scanner)) != null) return expr;
        if ((expr = attemptAccessVar(scanner)) != null) return expr;
        throw new SourceCodeException(scanner.peek().span(), "expected simple first");
    }

    private static HLExpression forceSimple(TokenScanner scanner) {
        HLExpression simple = forceSimpleFirst(scanner);
        Span span = simple.span;
        while (scanner.remaining() > 0) {
            if (scanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.DOT) {
                scanner.next();

                TokenTree access = scanner.next();
                span = Span.concat(span, access.span());
                if (access instanceof IdentToken ident) {
                    simple = new HLAccessPropertyName(span, simple, ident.value);
                } else if (access instanceof LiteralToken.Integer integer) {
                    throw new UnsupportedOperationException("tuples? screw you");
//                    simple = new HLAccess.Property.Index(span, simple, (int) integer.value, false);
                } else {
                    throw new SourceCodeException(access.span(), "expected literal/int in dot access");
                }

            } else if (scanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.R_ARROW) {
                scanner.next();

                span = Span.concat(span, punct.span());
                simple = new HLUnary(span, UnaryOperator.DEREF, simple);

                TokenTree access = scanner.next();
                span = Span.concat(span, access.span());
                if (access instanceof IdentToken ident) {
                    simple = new HLAccessPropertyName(span, simple, ident.value);
                } else if (access instanceof LiteralToken.Integer integer) {
                    throw new UnsupportedOperationException("haha tuples? screw you");
                } else {
                    throw new SourceCodeException(access.span(), "expected literal/int in arrow access");
                }

            } else if (scanner.peek() instanceof GroupToken group && group.type == GroupToken.Type.BRACKET) {
                span = Span.concat(span, scanner.next().span());
                TokenScanner indexScanner = new TokenScanner(group.value);
                HLExpression index = forceExpr(indexScanner);
                indexScanner.requireEmpty();
                throw new UnsupportedOperationException("index? screw you");
//                simple = new HLAccess.Index(span, simple, index, false);
            } else if (scanner.peek() instanceof GroupToken group && group.type == GroupToken.Type.PAREN) {
                span = Span.concat(span, scanner.next().span());
                TokenScanner paramScanner = new TokenScanner(group.value);
                List<HLExpression> params = new ArrayList<>();
                while (paramScanner.remaining() > 0) {
                    if (params.size() > 0) paramScanner.next().requirePunct(PunctToken.Type.COMMA);
                    params.add(forceExpr(paramScanner));
                }
                paramScanner.requireEmpty();
                simple = new HLCall(span, simple, params);
            } else if (scanner.peek() instanceof IdentToken ident && ident.value.equals("as")) {
                scanner.next();
//                HLType type = CommonParser.forceType(scanner);
//                span = Span.concat(span, scanner.lastConsumedSpan());
//                simple = new HLCast(span, simple, type);
                throw new UnsupportedOperationException("casts? screw you!");
            } else {
                break;
            }
        }
        return simple;
    }

    private static BinaryOperator binaryOperatorPeek(TokenScanner scanner) {
        if (scanner.remaining() > 0 && scanner.peek() instanceof PunctToken punct) {
            for (BinaryOperator operator : BinaryOperator.values()) {
                if (operator == BinaryOperator.DISCARD_FIRST) continue;
                if (operator.punctuation == punct.type) {
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
            result = new HLBinary(Span.concat(result.span, rhs.span), operator, result, rhs);
            operator = binaryOperatorPeek(scanner);
        }
        return result;
    }

    public static HLExpression forceExpr(TokenScanner scanner) {
        return binaryExpr(scanner, Integer.MIN_VALUE);
    }

    // https://eli.thegreenplace.net/2012/08/02/parsing-expressions-by-precedence-climbing

}
