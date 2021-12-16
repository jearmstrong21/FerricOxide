package mackycheese21.ferricoxide.parser;

import mackycheese21.ferricoxide.SourceCodeException;
import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.expr.Expression;
import mackycheese21.ferricoxide.ast.module.FOModule;
import mackycheese21.ferricoxide.ast.module.Function;
import mackycheese21.ferricoxide.ast.module.GlobalVariable;
import mackycheese21.ferricoxide.ast.stmt.Block;
import mackycheese21.ferricoxide.ast.type.ConcreteType;
import mackycheese21.ferricoxide.ast.type.FunctionType;
import mackycheese21.ferricoxide.ast.type.StructType;
import mackycheese21.ferricoxide.ast.type.TypeReference;
import mackycheese21.ferricoxide.parser.token.Token;
import mackycheese21.ferricoxide.parser.token.TokenScanner;

import java.util.ArrayList;
import java.util.List;

public class ModuleParser {

    private static final List<String> modTree = new ArrayList<>();

    public static Identifier currentModPath() {
        return new Identifier(modTree.toArray(new String[0]), true);
    }

    public static Identifier makeId(List<String> strings, boolean global) {
        String[] strs = new String[modTree.size() + strings.size()];
        for (int i = 0; i < modTree.size(); i++) {
            strs[i] = modTree.get(i);
        }
        for (int i = 0; i < strings.size(); i++) {
            strs[i + modTree.size()] = strings.get(i);
        }
        return new Identifier(strs, global);
    }

    // TODO: not public unless un-inline global = true
    private static Identifier makeId(String name) {
        String[] strs = new String[modTree.size() + 1];
        for (int i = 0; i < modTree.size(); i++) {
            strs[i] = modTree.get(i);
        }
        strs[modTree.size()] = name;
        return new Identifier(strs, true);
    }

    private static Function forceFunction(TokenScanner scanner) {
        boolean inline = scanner.peek().is(Token.Keyword.INLINE);
        if (inline) scanner.next();
        boolean extern = scanner.peek().is(Token.Keyword.EXTERN);
        String llvmName = null;
        if (extern) {
            scanner.next();
            if (scanner.peek().is(Token.Punctuation.L_PAREN)) {
                scanner.next();
                llvmName = scanner.next().string();
                scanner.next().mustBe(Token.Punctuation.R_PAREN);
            }
        }
        boolean export = scanner.peek().is(Token.Keyword.EXPORT);
        if (extern && export) throw new RuntimeException();
        if (export) {
            scanner.next();
            if (scanner.peek().is(Token.Punctuation.L_PAREN)) {
                scanner.next();
                llvmName = scanner.next().string();
                scanner.next().mustBe(Token.Punctuation.R_PAREN);
            }
        }
        scanner.next().mustBe(Token.Keyword.FN);
//        ConcreteType result = StatementParser.forceType(scanner);
        String name = scanner.next().identifier();
        if(llvmName == null && extern) { // if extern and unspecified name
            llvmName = name;
        }
        scanner.next().mustBe(Token.Punctuation.L_PAREN);
        List<String> paramNames = new ArrayList<>();
        List<ConcreteType> paramTypes = new ArrayList<>();
        while (!scanner.peek().is(Token.Punctuation.R_PAREN)) {
            if (paramTypes.size() > 0) scanner.next().mustBe(Token.Punctuation.COMMA);
            String paramName = scanner.next().identifier();
            scanner.next().mustBe(Token.Punctuation.COLON);
            ConcreteType type = StatementParser.forceType(scanner);
            paramNames.add(paramName);
            paramTypes.add(type);
        }
        scanner.next();
        ConcreteType result;
        if (scanner.peek().is(Token.Punctuation.ARROW)) {
            scanner.next();
            result = StatementParser.forceType(scanner);
        } else {
            result = ConcreteType.VOID;
        }
        FunctionType funcType = FunctionType.of(result, paramTypes);
        Block body;
        if (extern) {
            body = null;
        } else {
            body = StatementParser.forceBlock(scanner);
        }
        if (scanner.hasNext(Token.Punctuation.SEMICOLON)) scanner.next();
        // this identifier is global because its the true canonical identifier, not local
        // same for the attemptStruct makeId call
        return new Function(makeId(name), inline, funcType, paramNames, body, llvmName);
    }

    public static StructType attemptStruct(TokenScanner scanner) {
        TokenScanner s = scanner.copy();
        boolean packed = s.peek().is(Token.Keyword.PACKED);
        if (packed) s.next();
        if (!s.peek().is(Token.Keyword.STRUCT)) return null;
        s.next();
        if (!packed && !s.peek().is(Token.Type.IDENTIFIER)) return null;
        String name = s.next().identifier();
        List<String> fieldNames = new ArrayList<>();
        List<ConcreteType> fieldTypes = new ArrayList<>();

        if (s.peek().is(Token.Punctuation.L_BRACKET) || packed) {
            scanner.index = s.index;
            scanner.next().mustBe(Token.Punctuation.L_BRACKET);
            while (!scanner.peek().is(Token.Punctuation.R_BRACKET)) {
                fieldTypes.add(StatementParser.forceType(scanner));
                fieldNames.add(scanner.next().identifier());
                scanner.next().mustBe(Token.Punctuation.SEMICOLON);
            }
            scanner.next();
            if (scanner.hasNext(Token.Punctuation.SEMICOLON)) scanner.next();
            return new StructType(makeId(name), fieldNames, fieldTypes, packed);
        } else {
            return null;
        }
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
                return new GlobalVariable(type, makeId(name), value);
            }
        }
        return null;
    }

    private static void parseInto(TokenScanner scanner, FOModule module) {
        while (scanner.hasNext() && !scanner.hasNext(Token.Punctuation.R_BRACKET)) {
            if (scanner.hasNext(Token.Keyword.MOD)) {
                scanner.next();
                String name = scanner.next().identifier();
                scanner.next().mustBe(Token.Punctuation.L_BRACKET);
                modTree.add(name);
                parseInto(scanner, module);
                modTree.remove(modTree.size() - 1);
                scanner.next().mustBe(Token.Punctuation.R_BRACKET);
            } else {
                GlobalVariable global = attemptGlobal(scanner);
                if (global != null) {
                    module.globals.add(global);
                    continue;
                }

                StructType struct = attemptStruct(scanner);
                if (struct != null) {
                    module.structs.add(struct);
                    continue;
                }

                module.functions.add(forceFunction(scanner));
            }
        }
    }

    public static FOModule parse(TokenScanner scanner) {
        List<GlobalVariable> globals = new ArrayList<>();
        List<StructType> structs = new ArrayList<>();
        List<Function> functions = new ArrayList<>();
        FOModule module = new FOModule(globals, structs, functions);
        parseInto(scanner, module);
        return module;
    }

    public static Identifier attemptIdentifier(TokenScanner scanner) {
        TypeReference typeReference = attemptTypeReference(scanner);
        if (typeReference != null) return typeReference.identifier;
        return null;
    }

    public static TypeReference attemptTypeReference(TokenScanner scanner) {
        if (!scanner.hasNext(Token.Type.IDENTIFIER) && !scanner.hasNext(Token.Punctuation.DOUBLE_COLON)) return null;
        boolean global = false;
        if (scanner.hasNext(Token.Punctuation.DOUBLE_COLON)) {
            scanner.next();
            global = true;
        }
        List<String> id = new ArrayList<>();
        id.add(scanner.next().identifier());
        while (scanner.hasNext(Token.Punctuation.DOUBLE_COLON)) {
            scanner.next();
            id.add(scanner.next().identifier());
        }
        return new TypeReference(currentModPath(), new Identifier(id.toArray(String[]::new), global));
    }

    public static Identifier forceIdentifier(TokenScanner scanner) {
        Identifier identifier = attemptIdentifier(scanner);
        if (identifier == null) throw SourceCodeException.expectedIdentifier(scanner);
        return identifier;
    }

    public static TypeReference forceTypeReference(TokenScanner scanner) {
        TypeReference typeReference = attemptTypeReference(scanner);
        if (typeReference == null) throw SourceCodeException.expectedIdentifier(scanner);
        return typeReference;
    }
}
