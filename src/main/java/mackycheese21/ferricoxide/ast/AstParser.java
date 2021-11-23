package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.ConcreteType;
import mackycheese21.ferricoxide.Module;
import mackycheese21.ferricoxide.ast.token.Token;
import mackycheese21.ferricoxide.ast.token.TokenScanner;

import java.util.ArrayList;
import java.util.List;

public class AstParser {

    private static AstOpt<ConcreteType> type(TokenScanner scanner) {
        return AstOpt.from(() -> {
            Token token = scanner.next().unwrap(scanner);
            String identifier = token.identifier();
            switch (identifier) {
                case "i32":
                    return ConcreteType.I32;
                case "bool":
                    return ConcreteType.BOOL;
                case "void":
                    return ConcreteType.NONE;
            }
            throw new SourceCodeException(SourceCodeException.Type.EXPECTED_TYPE, token);
        });
    }

    private static AstOpt<Ast> number(TokenScanner scanner) {
        return AstOpt.from(() -> (Ast) new IntConstant((int) scanner.next().unwrap(scanner).integer())).suppressToNull();
    }

    private static AstOpt<Ast> var(TokenScanner scanner) {
        return AstOpt.from(() -> (Ast) new AccessVar(scanner.next().unwrap(scanner).identifier())).suppressToNull();
    }

    private static AstOpt<Ast> multiplicative(TokenScanner scanner) {
        return AstOpt.fromNullable(() -> {
            AstOpt<Ast> astOpt = simple(scanner);
            astOpt.propogateError();
            if (!astOpt.isPresent()) return null;
            Ast ast = astOpt.unwrap(scanner);
            while (scanner.hasNext(Token.Punctuation.STAR, Token.Punctuation.SLASH)) {
                Token.Punctuation p = scanner.next().unwrap(scanner).punctuation();
                Ast next = simple(scanner).unwrap(scanner);
                if (p == Token.Punctuation.STAR) {
                    ast = ArithBinary.Op.MUL.on(ast, next);
                } else {
                    ast = ArithBinary.Op.DIV.on(ast, next);
                }
            }
            return ast;
        });
    }

    private static AstOpt<Ast> additive(TokenScanner scanner) {
        return AstOpt.fromNullable(() -> {
            AstOpt<Ast> astOpt = multiplicative(scanner);
            astOpt.propogateError();
            if (!astOpt.isPresent()) return null;
            Ast ast = astOpt.unwrap(scanner);
            while (scanner.hasNext(Token.Punctuation.PLUS, Token.Punctuation.MINUS)) {
                Token.Punctuation p = scanner.next().unwrap(scanner).punctuation();
                Ast next = multiplicative(scanner).unwrap(scanner);
                if (p == Token.Punctuation.PLUS) {
                    ast = ArithBinary.Op.ADD.on(ast, next);
                } else {
                    ast = ArithBinary.Op.SUB.on(ast, next);
                }
            }
            return ast;
        });
    }

    private static AstOpt<Ast> comparative(TokenScanner scanner) {
        return AstOpt.fromNullable(() -> {
            AstOpt<Ast> astOpt = additive(scanner);
            astOpt.propogateError();
            if (!astOpt.isPresent()) return null;
            Ast ast = astOpt.unwrap(scanner);
            while (scanner.hasNext(Token.Punctuation.GE_EQ, Token.Punctuation.GE, Token.Punctuation.LE_EQ, Token.Punctuation.LE, Token.Punctuation.EQEQ)) {
                Token.Punctuation p = scanner.next().unwrap(scanner).punctuation();
                Ast next = additive(scanner).unwrap(scanner);
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
        });
    }

    private static AstOpt<Ast> paren(TokenScanner scanner) {
        return AstOpt.fromNullable(() -> {
            AstOpt<Token> lparen = scanner.next();
            lparen.propogateError();
            if (!lparen.isPresent()) return null;
            if (!lparen.unwrap(scanner).is(Token.Punctuation.L_PAREN)) return null;
            Ast ast = expr(scanner).unwrap(scanner);
            scanner.next().unwrap(scanner).mustBe(Token.Punctuation.R_PAREN);
            return ast;
        });
    }

    private static AstOpt<Ast> assign(TokenScanner scanner) {
        return AstOpt.fromNullable(() -> {
            AstOpt<Token> var = scanner.next();
            var.propogateError();
            if (!var.isPresent()) return null;
            if (!var.unwrapUnsafe().is(Token.Type.IDENTIFIER)) return null;
            String varName = var.unwrapUnsafe().identifier();

            AstOpt<Token> eq = scanner.next();
            eq.propogateError();
            if (!eq.isPresent()) return null;
            if (!eq.unwrapUnsafe().is(Token.Punctuation.EQ)) return null;

            return new AssignVar(varName, expr(scanner).unwrap(scanner));
        });
    }

    private static AstOpt<Ast> whileLoop(TokenScanner scanner) {
        return AstOpt.fromNullable(() -> {
            AstOpt<Token> whileToken = scanner.next();
            whileToken.propogateError();
            if (!whileToken.isPresent()) return null;
            if (!whileToken.unwrapUnsafe().is(Token.Type.IDENTIFIER)) return null;
            if (!whileToken.unwrapUnsafe().identifier().equals("while")) return null;
            Ast cond = expr(scanner).unwrap(scanner);
            Ast body = block(true, scanner).unwrap(scanner);
            return new WhileLoop(cond, body);
        });
    }

    private static AstOpt<Ast> returnStatement(TokenScanner scanner) {
        return AstOpt.fromNullable(() -> {
            AstOpt<Token> returnToken = scanner.next();
            returnToken.propogateError();
            if (!returnToken.isPresent()) return null;
            if (!returnToken.unwrapUnsafe().is(Token.Type.IDENTIFIER)) return null;
            if (!returnToken.unwrapUnsafe().identifier().equals("return")) return null;
            return new Return(expr(scanner).unwrap(scanner));
        });
    }

    private static AstOpt<Ast> simple(TokenScanner scanner) {
        return AstOpt.fromNullable(() -> {
            TokenScanner s;
            AstOpt<Ast> ast;

            s = scanner.copy();
            ast = whileLoop(s);
            ast.propogateError();
            if (ast.isPresent()) {
                scanner.index = s.index;
                return ast.unwrapUnsafe();
            }

            s = scanner.copy();
            ast = returnStatement(s);
            ast.propogateError();
            if (ast.isPresent()) {
                scanner.index = s.index;
                return ast.unwrapUnsafe();
            }

            s = scanner.copy();
            ast = assign(s);
            ast.propogateError();
            if (ast.isPresent()) {
                scanner.index = s.index;
                return ast.unwrapUnsafe();
            }

            s = scanner.copy();
            ast = block(false, s);
            ast.propogateError();
            if (ast.isPresent()) {
                scanner.index = s.index;
                return ast.unwrapUnsafe();
            }

            s = scanner.copy();
            ast = declare(s);
            ast.propogateError();
            if (ast.isPresent()) {
                scanner.index = s.index;
                return ast.unwrapUnsafe();
            }

            s = scanner.copy();
            ast = funcCall(s);
            ast.propogateError();
            if (ast.isPresent()) {
                scanner.index = s.index;
                return ast.unwrapUnsafe();
            }

            s = scanner.copy();
            ast = paren(s);
            ast.propogateError();
            if (ast.isPresent()) {
                scanner.index = s.index;
                return ast.unwrapUnsafe();
            }

            s = scanner.copy();
            ast = ifStatement(s);
            ast.propogateError();
            if (ast.isPresent()) {
                scanner.index = s.index;
                return ast.unwrapUnsafe();
            }

            s = scanner.copy();
            ast = number(s);
            ast.propogateError();
            if (ast.isPresent()) {
                scanner.index = s.index;
                return ast.unwrapUnsafe();
            }

            s = scanner.copy();
            ast = var(s);
            ast.propogateError();
            if (ast.isPresent()) {
                scanner.index = s.index;
                return ast.unwrapUnsafe();
            }

            return null;
        });
    }

    private static AstOpt<Ast> declare(TokenScanner scanner) {
        return AstOpt.fromNullable(() -> {
            AstOpt<ConcreteType> typeOpt = type(scanner);
            if (typeOpt.isPresent()) {
                ConcreteType type = typeOpt.unwrap(scanner);
                String identifier = scanner.next().unwrap(scanner).identifier();
                scanner.next().unwrap(scanner).is(Token.Punctuation.EQ);
                Ast expr = expr(scanner).unwrap(scanner);
                return new DeclareVar(type, identifier, expr);
            } else {
                return null;
            }
        });
    }

    private static AstOpt<Ast> expr(TokenScanner scanner) {
        return comparative(scanner);
    }

    private static AstOpt<Ast> block(boolean force, TokenScanner scanner) {
        return AstOpt.fromNullable(() -> {
            AstOpt<Token> token = scanner.next();
            token.propogateError();
            if (force) {
                token.unwrap(scanner).mustBe(Token.Punctuation.L_BRACKET);
            } else {
                if (!token.isPresent()) return null;
                if (!token.unwrap(scanner).is(Token.Punctuation.L_BRACKET)) return null;
            }
            List<Ast> asts = new ArrayList<>();
            while (true) {
                Token peek = scanner.peek().unwrap(scanner);
                if (peek.is(Token.Punctuation.R_BRACKET)) break;
                asts.add(expr(scanner).unwrap(scanner));
            }
            scanner.next().unwrap(scanner).mustBe(Token.Punctuation.R_BRACKET);
            return new Block(asts);
        });
    }

    // Are unwraps correct here? Maybe new AstOpt function for nullIfError
    //  or maybe an ifError(this, x) { if(this.error) { x } else { this } }
    private static Module.FunctionDecl.Param param(boolean commaFirst, TokenScanner scanner) {
        try {
            if (commaFirst) {
                scanner.next().unwrap(scanner).mustBe(Token.Punctuation.COMMA);
            }
            ConcreteType type = type(scanner).unwrap(scanner);
            String name = scanner.next().unwrap(scanner).identifier();
            return new Module.FunctionDecl.Param(name, type);
        } catch (SourceCodeException e) {
            return null;
        }
    }

    private static AstOpt<Ast> funcCall(TokenScanner scanner) {
        return AstOpt.fromNullable(() -> {
            String name = scanner.next().unwrap(scanner).identifier();
            scanner.next().unwrap(scanner).mustBe(Token.Punctuation.L_PAREN);
            List<Ast> args = new ArrayList<>();
            while (true) {
                try {
                    TokenScanner s = scanner.copy();
                    if (args.size() > 0) {
                        s.next().unwrap(scanner).mustBe(Token.Punctuation.COMMA);
                    }
                    Ast arg = expr(s).unwrap(scanner);
                    if (arg == null)
                        throw new SourceCodeException(SourceCodeException.Type.EXPECTED_EXPR, s.peek().unwrap(scanner));
                    scanner.index = s.index;
                    args.add(arg);
                } catch (SourceCodeException e) {
                    break;
                }
            }
            scanner.next().unwrap(scanner).mustBe(Token.Punctuation.R_PAREN);
            return (Ast) new FuncCall(name, args);
        }).suppressToNull();
    }

    private static AstOpt<Ast> ifStatement(TokenScanner scanner) {
        return AstOpt.fromNullable(() -> {
            Token kwd = scanner.next().unwrap(scanner);
            if (!kwd.is(Token.Type.IDENTIFIER) || !kwd.identifier().equals("if")) {
                return null;
            }
            Ast cond = expr(scanner).unwrap(scanner);
            Ast then = block(true, scanner).unwrap(scanner);
            kwd = scanner.next().unwrap(scanner);
            if (!kwd.is(Token.Type.IDENTIFIER) || !kwd.identifier().equals("else")) {
                throw new SourceCodeException(SourceCodeException.Type.EXPECTED_ELSE, kwd);
            }
            Ast otherwise = block(true, scanner).unwrap(scanner);
            return new If(cond, then, otherwise);
        });
    }

    // TODO make Token use AstOpt? or rewrite in non crappy lang
    private static AstOpt<Module.FunctionDecl> functionDecl(TokenScanner scanner) {
        return AstOpt.fromNullable(() -> {
            if (scanner.hasNext()) {
                boolean extern = false;
                AstOpt<Token> externToken = scanner.peek();
                externToken.propogateError();
                if (externToken.isPresent()) {
                    if (externToken.unwrapUnsafe().is(Token.Type.IDENTIFIER)) {
                        if (externToken.unwrapUnsafe().identifier().equals("extern")) {
                            scanner.next();
                            extern = true;
                        }
                    }
                }
                ConcreteType result = type(scanner).unwrap(scanner);
                String funcName = scanner.next().unwrap(scanner).identifier();
                scanner.next().unwrap(scanner).mustBe(Token.Punctuation.L_PAREN);
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
                scanner.next().unwrap(scanner).mustBe(Token.Punctuation.R_PAREN);
                Ast body = null;
                if (!extern) {
                    body = block(true, scanner).unwrap(scanner);
                }
                return new Module.FunctionDecl(funcName, params, body, result);
            } else {
                return null;
            }
        });
    }

    private static AstOpt<Module> module(TokenScanner scanner) {
        return AstOpt.from(() -> {
            Module module = new Module();
            while (true) {
                AstOpt<Module.FunctionDecl> fd = functionDecl(scanner);
                fd.propogateError();
                if (fd.isPresent()) {
                    module.functions.mapAdd(fd.unwrap(scanner).name, fd.unwrap(scanner));
                } else {
                    break;
                }
            }
            return module;
        });
    }

    public static Module parse(List<Token> data) throws SourceCodeException {
        return module(new TokenScanner(data)).unwrap(null);
    }

}
