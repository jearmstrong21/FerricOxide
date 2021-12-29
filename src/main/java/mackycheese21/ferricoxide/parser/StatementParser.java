package mackycheese21.ferricoxide.parser;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.SourceCodeException;
import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.expr.BinaryOperator;
import mackycheese21.ferricoxide.ast.expr.CallExpr;
import mackycheese21.ferricoxide.ast.expr.Expression;
import mackycheese21.ferricoxide.ast.stmt.*;
import mackycheese21.ferricoxide.ast.type.FOType;
import mackycheese21.ferricoxide.parser.token.Span;
import mackycheese21.ferricoxide.parser.token.Token;
import mackycheese21.ferricoxide.parser.token.TokenScanner;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class StatementParser {

    private static Block attemptBlock(TokenScanner scanner) {
        if (!scanner.peek().is(Token.Punctuation.L_BRACKET)) return null;
        Span start = scanner.spanAtRel(0);
        scanner.next();
        List<Statement> statements = new ArrayList<>();
        while (!scanner.peek().is(Token.Punctuation.R_BRACKET)) {
            statements.add(forceStatement(scanner));
        }
        scanner.next().mustBe(Token.Punctuation.R_BRACKET);
        if (scanner.hasNext() && scanner.peek().is(Token.Punctuation.SEMICOLON)) scanner.next();
        Span end = scanner.spanAtRel(-1);
        return new Block(Span.concat(start, end), statements);
    }

    public static @NotNull Block forceBlock(TokenScanner scanner) {
        Block block = attemptBlock(scanner);
        if (block == null) throw SourceCodeException.expectedBlock(scanner);
        return block;
    }

    private static Statement attemptIf(TokenScanner scanner) {
        if (!scanner.peek().is(Token.Keyword.IF)) return null;
        Span start = scanner.spanAtRel(0);
        scanner.next();
        scanner.next().mustBe(Token.Punctuation.L_PAREN);
        Expression condition = ExpressionParser.EXPR.parse(scanner);
        scanner.next().mustBe(Token.Punctuation.R_PAREN);
        Block then = forceBlock(scanner);
        if (scanner.hasNext() && scanner.peek().is(Token.Keyword.ELSE)) {
            scanner.next();
            Block otherwise = forceBlock(scanner);
            if (scanner.hasNext() && scanner.peek().is(Token.Punctuation.SEMICOLON)) scanner.next();
            Span end = scanner.spanAtRel(-1);
            return new IfStmt(Span.concat(start, end), condition, then, otherwise);
        } else {
            if (scanner.hasNext() && scanner.peek().is(Token.Punctuation.SEMICOLON)) scanner.next();
            Span end = scanner.spanAtRel(-1);
            return new IfStmt(Span.concat(start, end), condition, then, null);
        }
    }

    private static Statement attemptReturn(TokenScanner scanner) {
        if (!scanner.peek().is(Token.Keyword.RETURN)) return null;
        Span start = scanner.spanAtRel(0);
        scanner.next();
        if (scanner.peek().is(Token.Punctuation.SEMICOLON)) {
            scanner.next();
            Span end = scanner.spanAtRel(-1);
            return new ReturnStmt(Span.concat(start, end), null);
        }
        Expression expr = ExpressionParser.EXPR.parse(scanner);
        scanner.next().mustBe(Token.Punctuation.SEMICOLON);
        Span end = scanner.spanAtRel(-1);
        return new ReturnStmt(Span.concat(start, end), expr);
    }

    private static Statement attemptWhile(TokenScanner scanner) {
        if (!scanner.peek().is(Token.Keyword.WHILE)) return null;
        Span start = scanner.spanAtRel(0);
        scanner.next();
        scanner.next().mustBe(Token.Punctuation.L_PAREN);
        Expression condition = ExpressionParser.EXPR.parse(scanner);
        scanner.next().mustBe(Token.Punctuation.R_PAREN);
        Block body = forceBlock(scanner);
        if (scanner.hasNext() && scanner.peek().is(Token.Punctuation.SEMICOLON)) scanner.next();
        Span end = scanner.spanAtRel(-1);
        return new WhileStmt(Span.concat(start, end), condition, body);
    }

    private static Statement attemptDeclareVar(TokenScanner scanner) {
        TokenScanner s = scanner.copy();
        Span start = s.spanAtRel(0);
        FOType type = CommonParser.TYPE.parse(s);
        if (type == null) return null;
        if(!s.hasNext(Token.Type.IDENTIFIER)) return null;
        scanner.index = s.index;
        String name = scanner.next().identifier();
        Span idSpan = scanner.spanAtRel(-1);
        scanner.next().mustBe(Token.Punctuation.EQ);
        Expression value = ExpressionParser.EXPR.parse(scanner);
        scanner.next().mustBe(Token.Punctuation.SEMICOLON);
        Span end = scanner.spanAtRel(-1);
        return new DeclareVar(Span.concat(start, end), type, new Identifier(idSpan, name), value);
    }

    private static Statement attemptCall(TokenScanner scanner) {
        Expression expr = ExpressionParser.SIMPLE.parse(scanner);
//        if (callExpr == null) return null;
        scanner.next().mustBe(Token.Punctuation.SEMICOLON);
        Span end = scanner.spanAtRel(-1);
        if (expr instanceof CallExpr callExpr) return new CallStmt(Span.concat(callExpr.span, end), callExpr);
        // TODO: for loop doesnt require semicolon
        throw new SourceCodeException("expected call", end);
    }

    private static Statement attemptAssign(TokenScanner scanner) {
        TokenScanner s = scanner.copy();
        Span start = s.spanAtRel(0);
        Expression left;
        try {
            left = ExpressionParser.EXPR.parse(s);
        } catch (AnalysisException e) {
            if (s.peek().is(Token.Punctuation.EQ)) throw e;
            return null;
        }
        if (!s.peek().is(Token.Punctuation.EQ)) return null;
        s.next();
        scanner.index = s.index;
        Expression right = ExpressionParser.EXPR.parse(scanner);
        scanner.next().mustBe(Token.Punctuation.SEMICOLON);
        Span end = scanner.spanAtRel(-1);
        return new Assign(Span.concat(start, end), left, right, BinaryOperator.DISCARD_FIRST);
    }

    private static Statement attemptFor(TokenScanner scanner) {
        if (!scanner.hasNext(Token.Keyword.FOR)) return null;
        Span start = scanner.spanAtRel(0);
        scanner.next();
        scanner.next().mustBe(Token.Punctuation.L_PAREN);
        Statement init = parse(scanner);
        Expression condition = ExpressionParser.EXPR.parse(scanner);
        scanner.next().mustBe(Token.Punctuation.SEMICOLON);
        Statement update = parse(scanner);
        scanner.next().mustBe(Token.Punctuation.R_PAREN);
        Block block = forceBlock(scanner);
        Span end = scanner.spanAtRel(-1);
        return new ForStmt(Span.concat(start, end), init, condition, update, block);
    }

    private static Statement parse(TokenScanner scanner) {
        Statement stmt;

        stmt = attemptFor(scanner);
        if (stmt != null) return stmt;

        stmt = attemptBlock(scanner);
        if (stmt != null) return stmt;

        stmt = attemptIf(scanner);
        if (stmt != null) return stmt;

        stmt = attemptWhile(scanner);
        if (stmt != null) return stmt;

        stmt = attemptReturn(scanner);
        if (stmt != null) return stmt;

        stmt = attemptDeclareVar(scanner);
        if (stmt != null) return stmt;

        stmt = attemptAssign(scanner);
        if (stmt != null) return stmt;

        stmt = attemptCall(scanner);
        if (stmt != null) return stmt;

        return null;
    }

    public static @NotNull Statement forceStatement(TokenScanner scanner) {
        Statement statement = parse(scanner);
        if (statement == null) throw SourceCodeException.expectedStatement(scanner);
        return statement;
    }

}
