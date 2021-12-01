package mackycheese21.ferricoxide.parser;

import mackycheese21.ferricoxide.SourceCodeException;
import mackycheese21.ferricoxide.ast.expr.BinaryOperator;
import mackycheese21.ferricoxide.ast.expr.CallExpr;
import mackycheese21.ferricoxide.ast.expr.Expression;
import mackycheese21.ferricoxide.ast.stmt.*;
import mackycheese21.ferricoxide.ast.type.ConcreteType;
import mackycheese21.ferricoxide.ast.type.PointerType;
import mackycheese21.ferricoxide.ast.type.TypeReference;
import mackycheese21.ferricoxide.parser.token.Token;
import mackycheese21.ferricoxide.parser.token.TokenScanner;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class StatementParser {

    private static ConcreteType attemptTypeFirst(TokenScanner scanner) {
        if(scanner.peek().is(Token.Keyword.I8)) {
            scanner.next();
            return ConcreteType.I8;
        }
        if (scanner.peek().is(Token.Keyword.I32)) {
            scanner.next();
            return ConcreteType.I32;
        }
        if (scanner.peek().is(Token.Keyword.I64)) {
            scanner.next();
            return ConcreteType.I64;
        }
        if (scanner.peek().is(Token.Keyword.F32)) {
            scanner.next();
            return ConcreteType.F32;
        }
        if (scanner.peek().is(Token.Keyword.F64)) {
            scanner.next();
            return ConcreteType.F64;
        }
        if (scanner.peek().is(Token.Keyword.BOOL)) {
            scanner.next();
            return ConcreteType.BOOL;
        }
        if (scanner.peek().is(Token.Keyword.VOID)) {
            scanner.next();
            return ConcreteType.VOID;
        }
        if (scanner.peek().is(Token.Keyword.STRUCT)) {
            scanner.next();
            return new TypeReference(scanner.next().identifier());
        }
        return null;
    }

    public static ConcreteType attemptType(TokenScanner scanner) {
        ConcreteType type = attemptTypeFirst(scanner);
        if (type == null) return null;
        while (scanner.hasNext(Token.Punctuation.STAR)) {
            scanner.next();
            type = PointerType.of(type);
        }
        return type;
    }

    public static ConcreteType forceType(TokenScanner scanner) {
        ConcreteType type = attemptType(scanner);
        if (type == null) throw SourceCodeException.expectedType(scanner);
        return type;
    }

    private static Block attemptBlock(TokenScanner scanner) {
        if (!scanner.peek().is(Token.Punctuation.L_BRACKET)) return null;
        scanner.next();
        List<Statement> statements = new ArrayList<>();
        while (!scanner.peek().is(Token.Punctuation.R_BRACKET)) {
            statements.add(forceStatement(scanner));
        }
        scanner.next().mustBe(Token.Punctuation.R_BRACKET);
        if (scanner.hasNext() && scanner.peek().is(Token.Punctuation.SEMICOLON)) scanner.next();
        return new Block(statements);
    }

    public static @NotNull Block forceBlock(TokenScanner scanner) {
        Block block = attemptBlock(scanner);
        if (block == null) throw SourceCodeException.expectedBlock(scanner);
        return block;
    }

    private static Statement attemptIf(TokenScanner scanner) {
        if (!scanner.peek().is(Token.Keyword.IF)) return null;
        scanner.next();
        Expression condition = ExpressionParser.parse(scanner, false);
        Block then = forceBlock(scanner);
        if (scanner.hasNext() && scanner.peek().is(Token.Keyword.ELSE)) {
            scanner.next();
            Block otherwise = forceBlock(scanner);
            if (scanner.hasNext() && scanner.peek().is(Token.Punctuation.SEMICOLON)) scanner.next();
            return new IfStmt(condition, then, otherwise);
        } else {
            if (scanner.hasNext() && scanner.peek().is(Token.Punctuation.SEMICOLON)) scanner.next();
            return new IfStmt(condition, then, null);
        }
    }

    private static Statement attemptReturn(TokenScanner scanner) {
        if (!scanner.peek().is(Token.Keyword.RETURN)) return null;
        scanner.next();
        Expression expr = ExpressionParser.parse(scanner, false);
        scanner.next().mustBe(Token.Punctuation.SEMICOLON);
        return new ReturnStmt(expr);
    }

    private static Statement attemptWhile(TokenScanner scanner) {
        if (!scanner.peek().is(Token.Keyword.WHILE)) return null;
        scanner.next();
        Expression condition = ExpressionParser.parse(scanner, false);
        Block body = forceBlock(scanner);
        if (scanner.hasNext() && scanner.peek().is(Token.Punctuation.SEMICOLON)) scanner.next();
        return new WhileStmt(condition, body);
    }

    private static Statement attemptDeclareVar(TokenScanner scanner) {
        ConcreteType type = attemptType(scanner);
        if (type == null) return null;
        String name = scanner.next().identifier();
        scanner.next().mustBe(Token.Punctuation.EQ);
        Expression value = ExpressionParser.parse(scanner, false);
        scanner.next().mustBe(Token.Punctuation.SEMICOLON);
        return new DeclareVar(type, name, value);
    }

    private static Statement attemptCall(TokenScanner scanner) {
        CallExpr callExpr = ExpressionParser.attemptCall(scanner);
        if (callExpr == null) return null;
        scanner.next().mustBe(Token.Punctuation.SEMICOLON);
        return new CallStmt(callExpr);
    }

    private static Statement attemptAssign(TokenScanner scanner) {
        TokenScanner s = scanner.copy();
        Expression left = ExpressionParser.parse(s, true);
        if (!s.peek().is(Token.Punctuation.EQ)) return null;
        s.next();
        scanner.index = s.index;
        Expression right = ExpressionParser.parse(scanner, false);
        scanner.next().mustBe(Token.Punctuation.SEMICOLON);
        return new Assign(left, right, BinaryOperator.DISCARD_FIRST); // TODO += -= etc
    }

    private static Statement parse(TokenScanner scanner) {
        Statement stmt;

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

        stmt = attemptCall(scanner);
        if (stmt != null) return stmt;

        stmt = attemptAssign(scanner);
        if (stmt != null) return stmt; // TODO should this be null?

        return null;
    }

    public static @NotNull Statement forceStatement(TokenScanner scanner) {
        Statement statement = parse(scanner);
        if (statement == null) throw SourceCodeException.expectedStatement(scanner);
        return statement;
    }

}
