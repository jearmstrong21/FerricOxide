package mackycheese21.ferricoxide.parser;

import mackycheese21.ferricoxide.SourceCodeException;
import mackycheese21.ferricoxide.ast.BinaryOperator;
import mackycheese21.ferricoxide.ast.hl.expr.HLCallExpr;
import mackycheese21.ferricoxide.ast.hl.expr.HLExpression;
import mackycheese21.ferricoxide.ast.hl.stmt.*;
import mackycheese21.ferricoxide.ast.hl.type.HLType;
import mackycheese21.ferricoxide.parser.token.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class StatementParser {

    private static Span maybeOptionalSemicolon(TokenScanner scanner, boolean requireSemicolon) {
        if (requireSemicolon) {
            return scanner.next().requirePunct(PunctToken.Type.SEMICOLON).span();
        } else if (scanner.remaining() > 0 && scanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.SEMICOLON) {
            return scanner.next().span();
        }
        return null;
    }

    private static HLBlock attemptBlock(TokenScanner scanner) {
        if (scanner.peek() instanceof GroupToken group && group.type == GroupToken.Type.CURLY_BRACKET) {
            scanner.next();
            TokenScanner blockScanner = new TokenScanner(group.value);
            List<HLStatement> statements = new ArrayList<>();
            while (blockScanner.remaining() > 0) {
                statements.add(forceStatement(blockScanner, true));
            }
            blockScanner.requireEmpty();
            return new HLBlock(scanner.lastConsumedSpan(), statements);
        }
        return null;
    }

    public static @NotNull HLBlock forceBlock(TokenScanner scanner) {
        HLBlock block = attemptBlock(scanner);
        if (block == null) throw new SourceCodeException(scanner.peek().span(), "expected block");
        return block;
    }

    private static HLStatement attemptIf(TokenScanner scanner) {
        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("if")) {
            Span span = scanner.next().span();
            TokenScanner condScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.PAREN).value);
            HLExpression cond = ExpressionParser.forceExpr(condScanner);
            condScanner.requireEmpty();
            HLBlock then = forceBlock(scanner);
            span = Span.concat(span, then.span);
            if (scanner.peek() instanceof IdentToken elseIdent && elseIdent.value.equals("else")) {
                scanner.next();
                HLBlock otherwise = forceBlock(scanner);
                span = Span.concat(span, otherwise.span);
                return new HLIfStmt(span, cond, then.statements, otherwise.statements);
            }
            return new HLIfStmt(span, cond, then.statements, null);
        }
        return null;
    }

    private static HLStatement attemptReturn(TokenScanner scanner) {
        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("return")) {
            Span start = scanner.next().span();
            if (scanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.SEMICOLON) {
                Span end = scanner.next().span();
                return new HLReturn(Span.concat(start, end), null);
            }
            HLExpression expr = ExpressionParser.forceExpr(scanner);
            Span end = scanner.next().requirePunct(PunctToken.Type.SEMICOLON).span();
            return new HLReturn(Span.concat(start, end), expr);
        }
        return null;
    }

    private static HLStatement attemptWhile(TokenScanner scanner) {
        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("while")) {
            Span start = scanner.next().span();
            TokenScanner condScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.PAREN).value);
            HLExpression cond = ExpressionParser.forceExpr(condScanner);
            condScanner.requireEmpty();
            HLBlock body = forceBlock(scanner);
            Span end = scanner.lastConsumedSpan();
            return new HLWhile(Span.concat(start, end), cond, body.statements);
        }
        return null;
    }

    private static HLStatement attemptDeclareVar(TokenScanner scanner, boolean requireSemicolon) {
        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("let")) {
            Span start = scanner.next().span();
            String name = scanner.next().requireIdent().value;
            HLType type = null;
            if (scanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.COLON) {
                scanner.next();
                type = CommonParser.forceType(scanner);
            }
            scanner.next().requirePunct(PunctToken.Type.EQ);
            HLExpression value = ExpressionParser.forceExpr(scanner);
            Span end = Span.concat(scanner.lastConsumedSpan(), maybeOptionalSemicolon(scanner, requireSemicolon));
            return new HLDeclare(Span.concat(start, end), name, type, value);
        }
        return null;
    }

    private static HLStatement attemptLoop(TokenScanner scanner) {
        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("loop")) {
            scanner.next();
            Span start = scanner.lastConsumedSpan();
            HLBlock block = forceBlock(scanner);
            return new HLLoop(Span.concat(start, block.span), block.statements);
        }
        return null;
    }

    private static HLStatement attemptBreak(TokenScanner scanner) {
        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("break")) {
            Span start = scanner.next().span();
            Span end = scanner.next().requirePunct(PunctToken.Type.SEMICOLON).span();
            return new HLBreak(Span.concat(start, end));
        }
        return null;
    }

    private static HLStatement attemptCall(TokenScanner scanner, boolean requireSemicolon) {
        HLExpression expr = ExpressionParser.forceExpr(scanner);
        Span end = maybeOptionalSemicolon(scanner, requireSemicolon);
        Span span = Span.concat(expr.span, end);
        if (expr instanceof HLCallExpr callExpr) return new HLCallStmt(span, callExpr);
        throw new SourceCodeException(span, "expected call expr");
    }

    private static HLStatement attemptAssign(TokenScanner scanner, boolean requireSemicolon) {
        TokenScanner attempt = scanner.copy();
        HLExpression left;
        try {
            left = ExpressionParser.forceExpr(attempt);
        } catch (SourceCodeException e) {
            if (attempt.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.EQ) throw e;
            return null;
        }
        if (attempt.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.EQ) {
            attempt.next();
            scanner.from(attempt);
            HLExpression right = ExpressionParser.forceExpr(scanner);
            Span end = maybeOptionalSemicolon(scanner, requireSemicolon);
            return new HLAssign(Span.concat(left.span, end), left, right);
        }
        return null;
    }

    private static HLStatement attemptFor(TokenScanner scanner) {
        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("for")) {
            Span start = scanner.next().span();
            TokenScanner forScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.PAREN).value);
            HLStatement init = forceStatement(forScanner, true);
            HLExpression condition = ExpressionParser.forceExpr(forScanner);
            forScanner.next().requirePunct(PunctToken.Type.SEMICOLON);
            HLStatement update = forceStatement(forScanner, false);
            forScanner.requireEmpty();
            HLBlock block = forceBlock(scanner);
            return new HLFor(Span.concat(start, block.span), init, condition, update, block.statements);
        }
        return null;
    }

    public static HLStatement forceStatement(TokenScanner scanner, boolean requireSemicolon) {
        HLStatement stmt;
        if ((stmt = attemptFor(scanner)) != null) return stmt;
        if ((stmt = attemptBlock(scanner)) != null) return stmt;
        if ((stmt = attemptIf(scanner)) != null) return stmt;
        if ((stmt = attemptWhile(scanner)) != null) return stmt;
        if ((stmt = attemptReturn(scanner)) != null) return stmt;
        if ((stmt = attemptDeclareVar(scanner, requireSemicolon)) != null) return stmt;
        if ((stmt = attemptAssign(scanner, requireSemicolon)) != null) return stmt;
        if ((stmt = attemptLoop(scanner)) != null) return stmt;
        if ((stmt = attemptBreak(scanner)) != null) return stmt;
        if ((stmt = attemptCall(scanner, requireSemicolon)) != null) return stmt;
        throw new SourceCodeException(scanner.peek().span(), "expected statement");
    }

}
