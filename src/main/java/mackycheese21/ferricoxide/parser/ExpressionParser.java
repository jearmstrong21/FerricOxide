package mackycheese21.ferricoxide.parser;

import mackycheese21.ferricoxide.SourceCodeException;
import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.expr.*;
import mackycheese21.ferricoxide.ast.type.ConcreteType;
import mackycheese21.ferricoxide.parser.token.Token;
import mackycheese21.ferricoxide.parser.token.TokenScanner;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ExpressionParser {

    private static Expression attemptIf(TokenScanner scanner) {
        if (!scanner.peek().is(Token.Keyword.IF)) return null;
        scanner.next();
        Expression condition = parse(scanner, false);
        scanner.next().mustBe(Token.Punctuation.L_BRACKET);
        Expression then = parse(scanner, false);
        scanner.next().mustBe(Token.Punctuation.R_BRACKET);
        scanner.next().mustBe(Token.Keyword.ELSE);
        scanner.next().mustBe(Token.Punctuation.L_BRACKET);
        Expression otherwise = parse(scanner, false);
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
        Expression expr = parse(scanner, false);
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
        return new UnaryExpr(simple(scanner, true), operator);
    }

    private static Expression attemptBool(TokenScanner scanner) {
        if (!scanner.peek().is(Token.Keyword.TRUE, Token.Keyword.FALSE)) return null;
        return new BoolConstant(scanner.next().keyword() == Token.Keyword.TRUE);
    }

    public static CallExpr attemptCall(TokenScanner scanner) {
//        if (!scanner.peek().is(Token.Type.IDENTIFIER)) return null;
        TokenScanner s = scanner.copy();
        Expression function = simple(s, false).makeLValue();
//        String name = s.next().identifier();
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
            params.add(parse(scanner, false));
        }
        return new CallExpr(function, params);
    }

    private static Expression attemptAccessVar(TokenScanner scanner) {
        if (!scanner.peek().is(Token.Type.IDENTIFIER)) return null;
        Identifier identifier = ModuleParser.forceIdentifier(scanner);
        if (identifier.global) {
            // If explicitly global then its simply a canonical ID, nothing about it
            return new AccessVar(new Identifier[]{identifier});
        } else {
            // If its not explicitly global lets try it locally (BUT NOT GLOBALLY)
            if(!identifier.equals(Identifier.concat(false, identifier))) throw new RuntimeException(); // sanity check
            return new AccessVar(new Identifier[]{
                    Identifier.concat(false, identifier), // == identifier, this is here for clarity
                    Identifier.concat(true, ModuleParser.currentModPath(), identifier),
                    Identifier.concat(true, identifier),
            });
        }
    }

    private static Expression attemptSizeOf(TokenScanner scanner) {
        if (!scanner.peek().is(Token.Keyword.SIZEOF)) return null;
        scanner.next();
        scanner.next().mustBe(Token.Punctuation.L_PAREN);
        ConcreteType type = StatementParser.forceType(scanner);
        scanner.next().mustBe(Token.Punctuation.R_PAREN);
        return new SizeOf(type);
    }

    private static Expression attemptStructInit(TokenScanner scanner) {
        if (!scanner.peek().is(Token.Keyword.NEW)) return null;
        scanner.next();
        Identifier struct = ModuleParser.forceIdentifier(scanner);
        scanner.next().mustBe(Token.Punctuation.L_BRACKET);
        List<String> fieldNames = new ArrayList<>();
        List<Expression> fieldValues = new ArrayList<>();
        while (!scanner.peek().is(Token.Punctuation.R_BRACKET)) {
            if (fieldNames.size() > 0) scanner.next().mustBe(Token.Punctuation.COMMA);
            fieldNames.add(scanner.next().identifier());
            scanner.next().mustBe(Token.Punctuation.COLON);
            fieldValues.add(parse(scanner, false));
        }
        scanner.next();
        return new StructInit(struct, fieldNames, fieldValues);
    }

    private static Expression attemptCast(TokenScanner scanner) {
        TokenScanner s = scanner.copy();
        if (s.hasNext(Token.Punctuation.L_PAREN)) s.next();
        ConcreteType type = StatementParser.attemptType(s);
        if (type == null) return null;
        scanner.index = s.index;
        scanner.next().mustBe(Token.Punctuation.R_PAREN);
        return new CastExpr(type, simple(scanner, true));
    }

    private static Expression attemptString(TokenScanner scanner) {
        if (scanner.hasNext(Token.Type.STRING))
            return new StringConstant(StringConstant.unescape(scanner.next().string()));
        return null;
    }

    private static Expression attemptZeroinit(TokenScanner scanner) {
        if (!scanner.hasNext(Token.Keyword.ZEROINIT)) return null;
        scanner.next();
        scanner.next().mustBe(Token.Punctuation.L_PAREN);
        ConcreteType type = StatementParser.forceType(scanner);
        scanner.next().mustBe(Token.Punctuation.R_PAREN);
        return new ZeroInit(type);
    }

    private static Expression attemptFloat(TokenScanner scanner) {
        if (scanner.hasNext(Token.Type.DECIMAL)) return new FloatConstant((float) scanner.next().decimal());
        return null;
    }

    private static Expression simpleFirst(TokenScanner scanner, boolean canBeCall) {
        Expression expr;

//        System.out.println("simpleFirst " + scanner.peek());

        expr = attemptFloat(scanner);
        if (expr != null) return expr;

        expr = attemptZeroinit(scanner);
        if (expr != null) return expr;

        expr = attemptString(scanner);
        if (expr != null) return expr;

        expr = attemptStructInit(scanner);
        if (expr != null) return expr;

        expr = attemptSizeOf(scanner);
        if (expr != null) return expr;

        expr = attemptBool(scanner);
        if (expr != null) return expr;

        expr = attemptIf(scanner);
        if (expr != null) return expr;

        expr = attemptNumber(scanner);
        if (expr != null) return expr;

        expr = attemptCast(scanner);
        if (expr != null) return expr;

        expr = attemptParen(scanner);
        if (expr != null) return expr;

        expr = attemptUnaryExpr(scanner);
        if (expr != null) return expr;

        if (canBeCall) {
            expr = attemptCall(scanner);
            if (expr != null) return expr;
        }

        expr = attemptAccessVar(scanner);
        if (expr != null) return expr;

//        System.out.println("EXPECTED SIMPLE " + scanner.peek());
        throw SourceCodeException.expectedSimple(scanner);
    }

    private static Expression simple(TokenScanner scanner, boolean canBeCall) {
        int refs = 0;
        while (scanner.hasNext(Token.Punctuation.AND)) {
            scanner.next();
            refs++;
        }
        Expression simple = simpleFirst(scanner, canBeCall);
        while (scanner.hasNext(Token.Punctuation.PERIOD, Token.Punctuation.ARROW, Token.Punctuation.L_BRACE)) {
//            System.out.println("simple append to " + simple.stringify() + " with " + scanner.peek().punctuation());
            if (scanner.hasNext(Token.Punctuation.PERIOD)) {
                scanner.next();
//                System.out.println("\tfield = " + scanner.peek().identifier());
                simple = new AccessField(simple.makeLValue(), scanner.next().identifier());
            } else if (scanner.hasNext(Token.Punctuation.ARROW)) {
                scanner.next();
//                simple = new PointerDeref(simple);
                simple = new AccessField(simple, scanner.next().identifier());
            } else if (scanner.hasNext(Token.Punctuation.L_BRACE)) {
                scanner.next();
                simple = new AccessIndex(simple, parse(scanner, false));
                scanner.next().mustBe(Token.Punctuation.R_BRACE);
            }
        }
        for (int i = 0; i < refs; i++) { // FOR before IF because "int x = 5; &x = 3;" is legitimate and equivalent to "x = 3;"
            simple = simple.makeLValue();
        }
//        System.out.println("parsed simple " + simple.stringify());
        return simple;
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
        Expression result = simple(scanner, true);
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

    // NotNull
    public static @NotNull Expression parse(TokenScanner scanner, boolean requestLValue) {
        Expression expr = binaryExpr(scanner);
        if (!expr.lvalue && requestLValue) return expr.makeLValue();
        return expr;
    }

}
