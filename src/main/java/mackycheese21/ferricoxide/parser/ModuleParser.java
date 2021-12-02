package mackycheese21.ferricoxide.parser;

import mackycheese21.ferricoxide.ast.expr.Expression;
import mackycheese21.ferricoxide.ast.module.FOModule;
import mackycheese21.ferricoxide.ast.module.Function;
import mackycheese21.ferricoxide.ast.module.GlobalVariable;
import mackycheese21.ferricoxide.ast.stmt.Block;
import mackycheese21.ferricoxide.ast.type.ConcreteType;
import mackycheese21.ferricoxide.ast.type.FunctionType;
import mackycheese21.ferricoxide.ast.type.StructType;
import mackycheese21.ferricoxide.parser.token.Token;
import mackycheese21.ferricoxide.parser.token.TokenScanner;

import java.util.ArrayList;
import java.util.List;

public class ModuleParser {

    private static Function forceFunction(TokenScanner scanner) {
        if(scanner.hasNext(Token.Keyword.FUNC)) scanner.next();
        boolean inline = scanner.peek().is(Token.Keyword.INLINE);
        if (inline) scanner.next();
        boolean extern = scanner.peek().is(Token.Keyword.EXTERN);
        if (extern) scanner.next();
        ConcreteType result = StatementParser.forceType(scanner);
        String name = scanner.next().identifier();
        scanner.next().mustBe(Token.Punctuation.L_PAREN);
        List<ConcreteType> paramTypes = new ArrayList<>();
        List<String> paramNames = new ArrayList<>();
        while (!scanner.peek().is(Token.Punctuation.R_PAREN)) {
            if (paramTypes.size() > 0) scanner.next().mustBe(Token.Punctuation.COMMA);
            System.out.println(scanner.peek());
            System.out.println("attempting type");
            ConcreteType type = StatementParser.forceType(scanner);
            String paramName = scanner.next().identifier();
            paramTypes.add(type);
            paramNames.add(paramName);
        }
        scanner.next();
        FunctionType funcType = FunctionType.of(result, paramTypes);
        Block body;
        if (extern) {
            body = null;
        } else {
            body = StatementParser.forceBlock(scanner);
        }
        if (scanner.hasNext(Token.Punctuation.SEMICOLON)) scanner.next();
        return new Function(name, inline, funcType, paramNames, body);
    }

    public static StructType attemptStruct(TokenScanner scanner) {
        boolean packed = scanner.peek().is(Token.Keyword.PACKED);
        if (packed) {
            scanner.next().mustBe(Token.Keyword.STRUCT);
        } else {
            if (!scanner.peek().is(Token.Keyword.STRUCT)) return null;
            scanner.next();
        }

        String name = scanner.next().identifier();
        List<String> fieldNames = new ArrayList<>();
        List<ConcreteType> fieldTypes = new ArrayList<>();

        scanner.next().mustBe(Token.Punctuation.L_BRACKET);
        while (!scanner.peek().is(Token.Punctuation.R_BRACKET)) {
            fieldTypes.add(StatementParser.forceType(scanner));
            fieldNames.add(scanner.next().identifier());
            scanner.next().mustBe(Token.Punctuation.SEMICOLON);
        }
        scanner.next();
        if (scanner.hasNext(Token.Punctuation.SEMICOLON)) scanner.next();

        return new StructType(name, fieldNames, fieldTypes, packed);
    }

    private static GlobalVariable attemptGlobal(TokenScanner scanner) {
        TokenScanner s = scanner.copy();
        ConcreteType type = StatementParser.attemptType(s);
        if (type == null) return null;
        if (s.hasNext(Token.Type.IDENTIFIER)) {
            String name = s.next().identifier();
            if (s.hasNext(Token.Punctuation.EQ)) {
                s.next();
                scanner.index = s.index;
                Expression value = ExpressionParser.parse(scanner, false);
                scanner.next().mustBe(Token.Punctuation.SEMICOLON);
                return new GlobalVariable(type, name, value);
            }
        }
        return null;
    }

    public static FOModule parse(TokenScanner scanner) {
        List<GlobalVariable> globals = new ArrayList<>();
        List<StructType> structs = new ArrayList<>();
        List<Function> functions = new ArrayList<>();
        while (scanner.hasNext()) {
            GlobalVariable global = attemptGlobal(scanner);
            if (global != null) {
                globals.add(global);
                continue;
            }

            StructType struct = attemptStruct(scanner);
            if (struct != null) {
                structs.add(struct);
                continue;
            }

            functions.add(forceFunction(scanner));
        }
        return new FOModule(globals, structs, functions);
    }
    // TODO redo module ast / structure, preprocessor mayb?

}
