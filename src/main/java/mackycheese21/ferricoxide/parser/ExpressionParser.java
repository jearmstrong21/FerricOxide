package mackycheese21.ferricoxide.parser;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.SourceCodeException;
import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.Pair;
import mackycheese21.ferricoxide.ast.expr.*;
import mackycheese21.ferricoxide.ast.expr.unresolved.*;
import mackycheese21.ferricoxide.ast.type.FOType;
import mackycheese21.ferricoxide.parser.token.Span;
import mackycheese21.ferricoxide.parser.token.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionParser {

    private static final CommonParser.Parse<Expression> IF = AlternativeSkipException.process(scanner -> {
        Span start = scanner.spanAtRel(0);
        scanner.next().skipIfNot(Token.Keyword.IF);
        scanner.next().mustBe(Token.Punctuation.L_PAREN);
        Expression condition = ExpressionParser.EXPR.parse(scanner);
        scanner.next().mustBe(Token.Punctuation.R_PAREN);
        scanner.next().mustBe(Token.Punctuation.L_BRACKET);
        Expression then = ExpressionParser.EXPR.parse(scanner);
        scanner.next().mustBe(Token.Punctuation.R_BRACKET);
        scanner.next().mustBe(Token.Keyword.ELSE);
        scanner.next().mustBe(Token.Punctuation.L_BRACKET);
        Expression otherwise = ExpressionParser.EXPR.parse(scanner);
        scanner.next().mustBe(Token.Punctuation.R_BRACKET);
        Span end = scanner.spanAtRel(-1);
        return new IfExpr(Span.concat(start, end), condition, then, otherwise);
    });
    private static final CommonParser.Parse<Expression> LITERAL = AlternativeSkipException.process(scanner -> {
        if (scanner.hasNext(Token.Type.INTEGER)) {
            return new UnresolvedIntConstant(scanner.spanAtRel(0), scanner.next().integer());
        }
        if (scanner.hasNext(Token.Type.DECIMAL)) {
            return new UnresolvedFloatConstant(scanner.spanAtRel(0), scanner.next().decimal());
        }
        if (scanner.hasNext(Token.Keyword.TRUE)) {
            scanner.next();
            return new BoolConstant(scanner.spanAtRel(0), true);
        }
        if (scanner.hasNext(Token.Keyword.FALSE)) {
            scanner.next();
            return new BoolConstant(scanner.spanAtRel(0), false);
        }
        if (scanner.hasNext(Token.Type.STRING)) {
            return new StringConstant(scanner.spanAtRel(0), scanner.next().string());
        }
        return null;
    });
    private static final CommonParser.Parse<Expression> PAREN = AlternativeSkipException.process(scanner -> {
        scanner.next().skipIfNot(Token.Punctuation.L_PAREN);
        Expression expr = ExpressionParser.EXPR.parse(scanner);
        scanner.next().mustBe(Token.Punctuation.R_PAREN);
        return expr;
    });
    private static final CommonParser.Parse<Expression> UNARY = AlternativeSkipException.process(scanner -> {
        Span start = scanner.spanAtRel(0);
        Token.Punctuation punctuation = scanner.next().punctuationOrSkip();
        for (UnaryOperator operator : UnaryOperator.values()) {
            if (operator.punctuation == punctuation) {
                Expression expr = ExpressionParser.EXPR.parse(scanner);
                return new Unary(Span.concat(start, expr.span), expr, operator);
            }
        }
        return null;
    });
    private static final CommonParser.Parse<Expression> ACCESS_VAR = AlternativeSkipException.process(scanner -> {
        boolean ref = scanner.hasNext(Token.Punctuation.AND);
        if (ref) scanner.next();
        Identifier identifier = CommonParser.IDENTIFIER.parse(scanner);
        if (identifier == null) {
            if (!ref) throw new AlternativeSkipException();
            else throw new SourceCodeException("expected identifier after ref", scanner.currentOrLast().span);
        }
        return new UnresolvedAccessVar(identifier.span, ref, identifier);
    });
    private static final CommonParser.Parse<Expression> SIZEOF = AlternativeSkipException.process(scanner -> {
        Span start = scanner.spanAtRel(0);
        scanner.next().skipIfNot(Token.Keyword.SIZEOF);
        scanner.next().mustBe(Token.Punctuation.L_PAREN);
        FOType type = CommonParser.TYPE.parse(scanner);
        scanner.next().mustBe(Token.Punctuation.R_PAREN);
        Span end = scanner.spanAtRel(-1);
        return new SizeOf(Span.concat(start, end), type);
    });
    private static final CommonParser.Parse<Expression> STRUCT_INIT = AlternativeSkipException.process(scanner -> {
        Span start = scanner.spanAtRel(0);
        Identifier struct = CommonParser.IDENTIFIER.parse(scanner);
        if (struct == null) return null;
        scanner.next().skipIfNot(Token.Punctuation.L_BRACKET);
        List<Pair<String, Expression>> initializer = CommonParser.commaSeparatedList(scanner1 -> {
            String name = scanner1.next().identifierOrSkip();
            scanner1.next().mustBe(Token.Punctuation.COLON);
            return new Pair<>(name, ExpressionParser.EXPR.parse(scanner1));
        }, CommonParser.COMMA).parse(scanner);
        scanner.next().mustBe(Token.Punctuation.R_BRACKET);
        Span end = scanner.spanAtRel(-1);
        return new UnresolvedStructInit(Span.concat(start, end), struct, initializer);
    });
    private static final CommonParser.Parse<Expression> ZEROINIT = AlternativeSkipException.process(scanner -> {
        Span start = scanner.spanAtRel(0);
        scanner.next().skipIfNot(Token.Keyword.ZEROINIT);
        scanner.next().mustBe(Token.Punctuation.L_PAREN);
        FOType type = CommonParser.TYPE.parse(scanner);
        scanner.next().mustBe(Token.Punctuation.R_PAREN);
        Span end = scanner.spanAtRel(-1);
        return new ZeroInit(Span.concat(start, end), type);
    });

    private static final CommonParser.Parse<Expression> SIMPLE_FIRST = CommonParser.alt("expected simple",
            PAREN,
            LITERAL,

            ZEROINIT,
            SIZEOF,

            IF,

            UNARY,
            STRUCT_INIT,
            ACCESS_VAR
    );
    public static final CommonParser.Parse<Expression> SIMPLE = scanner -> {
        Span start = scanner.spanAtRel(0);
        boolean ref = scanner.hasNext(Token.Punctuation.AND);
        if (ref) scanner.next();
        Expression simple = SIMPLE_FIRST.parse(scanner);
        while (scanner.hasNext(Token.Punctuation.PERIOD, Token.Punctuation.ARROW, Token.Punctuation.L_BRACE, Token.Punctuation.L_PAREN) || scanner.hasNext(Token.Keyword.AS)) {
            if (scanner.hasNext(Token.Punctuation.PERIOD)) {

                scanner.next();
                FOType.Access access = FOType.Access.string(scanner.next().identifier());
                simple = new UnresolvedAccessProperty(Span.concat(start, scanner.spanAtRel(-1)), simple, access, false, false);

            } else if (scanner.hasNext(Token.Punctuation.ARROW)) {

                scanner.next();
                FOType.Access access = FOType.Access.string(scanner.next().identifier());
                simple = new UnresolvedAccessProperty(Span.concat(start, scanner.spanAtRel(-1)), simple, access, false, false);

            } else if (scanner.hasNext(Token.Punctuation.L_BRACE)) {

                scanner.next();
                Expression index = ExpressionParser.EXPR.parse(scanner);
                Span end = scanner.spanAtRel(0);
                simple = new ArrayIndex(Span.concat(start, end), simple, index, false);
                scanner.next().mustBe(Token.Punctuation.R_BRACE);

            } else if (scanner.hasNext(Token.Punctuation.L_PAREN)) {
                scanner.next();
                List<Expression> params = CommonParser.commaSeparatedList(ExpressionParser.EXPR, CommonParser.COMMA).parse(scanner);
                simple = new CallExpr(Span.concat(simple.span, scanner.spanAtRel(0)), simple, params);
                scanner.next().mustBe(Token.Punctuation.R_PAREN);
            } else if (scanner.hasNext(Token.Keyword.AS)) {
                scanner.next();
                FOType type = CommonParser.TYPE.parse(scanner);
                simple = new CastExpr(Span.concat(simple.span, scanner.spanAtRel(-1)), type, simple);
            } else {
                throw new AssertionError();
            }
        }
        if (ref) {
            if (simple instanceof UnresolvedAccessProperty uap) {
                uap.explicitRef = true;
            } else if (simple instanceof UnresolvedAccessVar uav) {
                uav.explicitRef = true;
            } else if (simple instanceof ArrayIndex ai) {
                ai.ref = true;
            } else {
                // TODO this code is ugly as FUCK dude
                throw new AnalysisException(simple.span, "cannot take reference");
            }
        }
        return simple;
    };

    private static final CommonParser.Parse<BinaryOperator> BINARY_OPERATOR_PEEK = AlternativeSkipException.process(scanner -> {
        Token.Punctuation punctuation = scanner.peek().punctuationOrSkip();
        for (BinaryOperator operator : BinaryOperator.values()) {
            if (operator.punctuation == punctuation) {
                return operator;
            }
        }
        return null;
    });

    private static final Map<Integer, CommonParser.Parse<Expression>> BINARY_EXPR_CACHE = new HashMap<>();

    private static CommonParser.Parse<Expression> binaryExpr(int minPriority) {
        if (!BINARY_EXPR_CACHE.containsKey(minPriority)) {
            BINARY_EXPR_CACHE.put(minPriority, scanner -> {
                Expression result = SIMPLE.parse(scanner);
                BinaryOperator operator = BINARY_OPERATOR_PEEK.parse(scanner);
                while (operator != null && operator.priority >= minPriority) {
                    scanner.next();
                    Expression rhs = binaryExpr(operator.priority + 1).parse(scanner);
                    result = new Binary(Span.concat(result.span, rhs.span), result, rhs, operator);
                    operator = BINARY_OPERATOR_PEEK.parse(scanner);
                }
                return result;
            });
        }
        return BINARY_EXPR_CACHE.get(minPriority);
    }

    public static final CommonParser.Parse<Expression> EXPR = binaryExpr(0);

    // https://eli.thegreenplace.net/2012/08/02/parsing-expressions-by-precedence-climbing

}
