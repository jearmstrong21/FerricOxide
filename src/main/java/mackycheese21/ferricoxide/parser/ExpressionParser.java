package mackycheese21.ferricoxide.parser;

import mackycheese21.ferricoxide.SourceCodeException;
import mackycheese21.ferricoxide.ast.expr.BinaryOperator;
import mackycheese21.ferricoxide.ast.UnaryOperator;
import mackycheese21.ferricoxide.ast.expr.*;
import mackycheese21.ferricoxide.parser.token.Token;
import mackycheese21.ferricoxide.parser.token.TokenScanner;

import java.util.ArrayList;
import java.util.List;

public class ExpressionParser {

    private static Expression attemptIf(TokenScanner scanner) {
        if (!scanner.peek().is(Token.Keyword.IF)) return null;
        scanner.next();
        Expression condition = parse(scanner);
        scanner.next().mustBe(Token.Punctuation.L_BRACKET);
        Expression then = parse(scanner);
        scanner.next().mustBe(Token.Punctuation.R_BRACKET);
        scanner.next().mustBe(Token.Keyword.ELSE);
        scanner.next().mustBe(Token.Punctuation.L_BRACKET);
        Expression otherwise = parse(scanner);
        scanner.next().mustBe(Token.Punctuation.R_BRACKET);
        return new IfExpr(condition, then, otherwise);
    }

    private static Expression attemptNumber(TokenScanner scanner) {
        if (!scanner.peek().is(Token.Type.INTEGER)) return null;
        return new IntConstant((int) scanner.next().integer());
    }

    private static Expression attemptParen(TokenScanner scanner) {
        if (!scanner.peek().is(Token.Punctuation.L_PAREN)) return null;
        scanner.next();
        Expression expr = parse(scanner);
        scanner.next().mustBe(Token.Punctuation.R_PAREN);
        return expr;
    }

    private static UnaryOperator attemptUnaryOperator(TokenScanner scanner) {
        if (!scanner.peek().is(Token.Type.PUNCTUATION)) return null;
        for (UnaryOperator operator : UnaryOperator.values()) {
            if (operator.punctuation == scanner.peek().punctuation()) {
                scanner.next();
                return operator;
            }
        }
        return null;
    }

    private static Expression attemptUnaryExpr(TokenScanner scanner) {
        UnaryOperator operator = attemptUnaryOperator(scanner);
        if (operator == null) return null;
        return new UnaryExpr(simple(scanner), operator);
    }

    private static Expression attemptBool(TokenScanner scanner) {
        if (!scanner.peek().is(Token.Keyword.TRUE, Token.Keyword.FALSE)) return null;
        return new BoolConstant(scanner.next().keyword() == Token.Keyword.TRUE);
    }

    public static CallExpr attemptCall(TokenScanner scanner) {
        if (!scanner.peek().is(Token.Type.IDENTIFIER)) return null;
        TokenScanner s = scanner.copy();
        String name = s.next().identifier();
        if (!s.peek().is(Token.Punctuation.L_PAREN)) return null;
        s.next();
        scanner.index = s.index;
        List<Expression> params = new ArrayList<>();
        while (true) {
            if (scanner.peek().is(Token.Punctuation.R_PAREN)) {
                scanner.next();
                break;
            }
            if (params.size() > 0) {
                scanner.next().mustBe(Token.Punctuation.COMMA);
            }
            params.add(parse(scanner));
        }
        return new CallExpr(name, params);
    }

    private static Expression attemptAccessVar(TokenScanner scanner) {
        if (!scanner.peek().is(Token.Type.IDENTIFIER)) return null;
        return new AccessVar(scanner.next().identifier());
    }

    private static Expression simple(TokenScanner scanner) {
        Expression expr;

        expr = attemptBool(scanner);
        if (expr != null) return expr;

        expr = attemptIf(scanner);
        if (expr != null) return expr;

        expr = attemptNumber(scanner);
        if (expr != null) return expr;

        expr = attemptParen(scanner);
        if (expr != null) return expr;

        expr = attemptUnaryExpr(scanner);
        if (expr != null) return expr;

        expr = attemptCall(scanner);
        if (expr != null) return expr;

        expr = attemptAccessVar(scanner);
        if (expr != null) return expr;

        throw SourceCodeException.expectedSimple(scanner);
    }

    private static BinaryOperator peekBinaryOperator(TokenScanner scanner) {
        if (!scanner.hasNext() || !scanner.peek().is(Token.Type.PUNCTUATION)) return null;
        for (BinaryOperator operator : BinaryOperator.values()) {
            if (operator.punctuation == scanner.peek().punctuation()) {
                return operator;
            }
        }
        return null;
    }

    // https://eli.thegreenplace.net/2012/08/02/parsing-expressions-by-precedence-climbing
    private static Expression binaryExpr(TokenScanner scanner, int minPriority) {
        Expression result = simple(scanner);
        BinaryOperator operator = peekBinaryOperator(scanner);
        while (operator != null && operator.priority >= minPriority) {
            scanner.next();
            Expression rhs = binaryExpr(scanner, operator.priority + 1);
            result = new BinaryExpr(result, rhs, operator);
            operator = peekBinaryOperator(scanner);
        }
        return result;
    }

    private static Expression binaryExpr(TokenScanner scanner) {
        return binaryExpr(scanner, 0);
    }

//    // https://en.wikipedia.org/wiki/Operator-precedence_parser
//    private static Expression binaryExpr(TokenScanner scanner, Expression lhs, int minPriority) {
//        BinaryOperator lookahead = peekBinaryOperator(scanner);
//        while (lookahead != null && lookahead.priority >= minPriority) {
//            BinaryOperator op = lookahead;
//            scanner.next();
//            Expression rhs = parse(scanner);
//            lookahead = peekBinaryOperator(scanner);
//            while (lookahead != null && lookahead.priority >= op.priority) {
//                rhs = binaryExpr(scanner, rhs, op.priority + 1);
//                lookahead = peekBinaryOperator(scanner);
//            }
//            lhs = new BinaryExpr(lhs, rhs, op);
//        }
//        return lhs;
//    }
//
//    private static Expression binaryExpr(TokenScanner scanner) {
//        return binaryExpr(scanner, simple(scanner), 0);
//    }

    // NotNull
    public static Expression parse(TokenScanner scanner) {
        return binaryExpr(scanner);
    }

}
