package mackycheese21.ferricoxide.token;

import mackycheese21.ferricoxide.ConcreteType;
import mackycheese21.ferricoxide.Module;
import mackycheese21.ferricoxide.ast.*;

import java.util.ArrayList;
import java.util.List;

public class AstParser {

    private static ConcreteType type(TokenScanner scanner) throws ParseException, TokenException {
        Token token = scanner.next();
        String identifier = token.identifier();
        switch (identifier) {
            case "i32":
                return ConcreteType.I32;
            case "void":
                return ConcreteType.NONE;
            case "bool":
                return ConcreteType.BOOL;
        }
        throw new ParseException(ParseException.Type.EXPECTED_TYPE, token);
    }

    private static Ast number(TokenScanner scanner) throws TokenException {
        return new IntConstant((int) scanner.next().integer());
    }

    private static Ast var(TokenScanner scanner) throws TokenException {
        return new AccessVar(scanner.next().identifier());
    }

    private static Ast comparative(TokenScanner scanner) throws TokenException, ParseException {
        Ast ast = additive(scanner);
        while (scanner.hasNext(Token.Punctuation.GE_EQ, Token.Punctuation.GE, Token.Punctuation.LE_EQ, Token.Punctuation.LE, Token.Punctuation.EQEQ)) {
            Token.Punctuation p = scanner.next().punctuation();
            Ast next = additive(scanner);
            switch (p) {
                case GE_EQ:
                    ast = ArithBinary.Op.GE.on(ast, next);
                    break;
                case GE:
                    ast = ArithBinary.Op.GT.on(ast, next);
                    break;
                case LE_EQ:
                    ast = ArithBinary.Op.LE.on(ast, next);
                    break;
                case LE:
                    ast = ArithBinary.Op.LT.on(ast, next);
                    break;
                case EQEQ:
                    ast = ArithBinary.Op.EQ.on(ast, next);
                    break;
                default:
                    throw new UnsupportedOperationException(p.toString());
            }
        }
        return ast;
    }

    private static Ast additive(TokenScanner scanner) throws TokenException, ParseException {
        Ast ast = multiplicative(scanner);
        while (scanner.hasNext(Token.Punctuation.PLUS, Token.Punctuation.MINUS)) {
            Token.Punctuation p = scanner.next().punctuation();
            Ast next = multiplicative(scanner);
            if (p == Token.Punctuation.PLUS) {
                ast = ArithBinary.Op.ADD.on(ast, next);
            } else {
                ast = ArithBinary.Op.SUB.on(ast, next);
            }
        }
        return ast;
    }

    private static Ast multiplicative(TokenScanner scanner) throws TokenException, ParseException {
        Ast ast = simple(scanner);
        while (scanner.hasNext(Token.Punctuation.STAR, Token.Punctuation.SLASH)) {
            Token.Punctuation p = scanner.next().punctuation();
            Ast next = simple(scanner);
            if (p == Token.Punctuation.STAR) {
                ast = ArithBinary.Op.MUL.on(ast, next);
            } else {
                ast = ArithBinary.Op.DIV.on(ast, next);
            }
        }
        return ast;
    }

    private static Ast paren(TokenScanner scanner) throws TokenException, ParseException {
        scanner.next().mustBe(Token.Punctuation.L_PAREN);
        Ast ast = expr(scanner);
        scanner.next().mustBe(Token.Punctuation.R_PAREN);
        return ast;
    }

    private static Ast simple(TokenScanner scanner) throws TokenException, ParseException {
        try {
            TokenScanner s = scanner.copy();
            Ast ast = funcCall(s);
            scanner.index = s.index;
            return ast;
        } catch (TokenException | ParseException e) {

        }
        try {
            TokenScanner s = scanner.copy();
            Ast ast = paren(s);
            scanner.index = s.index;
            return ast;
        } catch (TokenException | ParseException e) {

        }
        try {
            TokenScanner s = scanner.copy();
            Ast ast = ifStatement(s);
            scanner.index = s.index;
            return ast;
        } catch (TokenException | ParseException e) {

        } catch (NoBacktrackIndicatorException e) {
            throw e.wrapped;
        }
        try {
            TokenScanner s = scanner.copy();
            Ast ast = number(s);
            scanner.index = s.index;
            return ast;
        } catch (TokenException e) {

        }
        try {
            TokenScanner s = scanner.copy();
            Ast ast = var(s);
            scanner.index = s.index;
            return ast;
        } catch (TokenException e) {

        }
        throw new ParseException(ParseException.Type.EXPECTED_SIMPLE, scanner.peek());
    }

    private static Ast expr(TokenScanner scanner) throws TokenException, ParseException {
        return comparative(scanner);
    }

    private static Ast block(TokenScanner scanner) throws TokenException, ParseException {
        scanner.next().mustBe(Token.Punctuation.L_BRACKET);
        Ast ast = expr(scanner);
        scanner.next().mustBe(Token.Punctuation.R_BRACKET);
        return ast;
    }

    private static Module.FunctionDecl.Param param(boolean commaFirst, TokenScanner scanner) {
        try {
            if (commaFirst) {
                scanner.next().mustBe(Token.Punctuation.COMMA);
            }
            ConcreteType type = type(scanner);
            String name = scanner.next().identifier();
            return new Module.FunctionDecl.Param(name, type);
        } catch (ParseException | TokenException e) {
            return null;
        }
    }

    private static Ast funcCall(TokenScanner scanner) throws TokenException, ParseException {
        String name = scanner.next().identifier();
        scanner.next().mustBe(Token.Punctuation.L_PAREN);
        List<Ast> args = new ArrayList<>();
        while (true) {
            try {
                TokenScanner s = scanner.copy();
                if (args.size() > 0) {
                    s.next().mustBe(Token.Punctuation.COMMA);
                }
                Ast arg = expr(s);
                if (arg == null) throw new ParseException(ParseException.Type.INTERNAL, s);
                scanner.index = s.index;
                args.add(arg);
            } catch (TokenException | ParseException e) {
                break;
            }
        }
        scanner.next().mustBe(Token.Punctuation.R_PAREN);
        return new FuncCall(name, args);
    }

    private static Ast ifStatement(TokenScanner scanner) throws TokenException, ParseException, NoBacktrackIndicatorException {
        Token kwd = scanner.next();
        if (!kwd.identifier().equals("if")) {
            throw new ParseException(ParseException.Type.INTERNAL, kwd);
        }
        // This try has no functional difference, just improves error handling (no backtracking after `if`)
        try {
            Ast cond = expr(scanner);
            Ast then = block(scanner);
            kwd = scanner.next();
            if (!kwd.is(Token.Type.IDENTIFIER) || !kwd.identifier().equals("else")) {
                throw new ParseException(ParseException.Type.EXPECTED_ELSE, kwd);
            }
            Ast otherwise = block(scanner);
            return new If(cond, then, otherwise);
        } catch (ParseException e) {
            throw new NoBacktrackIndicatorException(e);
        }
    }

    private static Module.FunctionDecl functionDecl(TokenScanner scanner) throws ParseException, TokenException {
        if (scanner.hasNext()) {
            ConcreteType result = type(scanner);
            String funcName = scanner.next().identifier();
            scanner.next().mustBe(Token.Punctuation.L_PAREN);
            List<Module.FunctionDecl.Param> params = new ArrayList<>();
            while (true) {
                TokenScanner s = scanner.copy();
                Module.FunctionDecl.Param p = param(params.size() > 0, s);
                if (p != null) {
                    scanner.index = s.index;
                    params.add(p);
                } else {
                    break;
                }
            }
            scanner.next().mustBe(Token.Punctuation.R_PAREN);
            Ast body = block(scanner);
            return new Module.FunctionDecl(funcName, params, body, result);
        } else {
            return null;
        }
    }

    private static Module module(TokenScanner scanner) throws TokenException, ParseException {
        Module module = new Module();
        while (true) {
            Module.FunctionDecl fd = functionDecl(scanner);
            if (fd != null) {
                module.functions.mapAdd(fd.name, fd);
            } else {
                break;
            }
        }
        return module;
    }

    public static Module parse(List<Token> data) throws TokenException, ParseException {
        return module(new TokenScanner(data));
    }

}
