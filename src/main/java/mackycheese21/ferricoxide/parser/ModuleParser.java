package mackycheese21.ferricoxide.parser;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.SourceCodeException;
import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.expr.Expression;
import mackycheese21.ferricoxide.ast.module.FOModule;
import mackycheese21.ferricoxide.ast.module.Function;
import mackycheese21.ferricoxide.ast.module.GlobalVariable;
import mackycheese21.ferricoxide.ast.stmt.Block;
import mackycheese21.ferricoxide.ast.type.FOType;
import mackycheese21.ferricoxide.ast.type.FunctionType;
import mackycheese21.ferricoxide.ast.type.StructType;
import mackycheese21.ferricoxide.ast.type.UnresolvedType;
import mackycheese21.ferricoxide.parser.token.Span;
import mackycheese21.ferricoxide.parser.token.Token;
import mackycheese21.ferricoxide.parser.token.TokenScanner;

import java.util.ArrayList;
import java.util.List;

public class ModuleParser {
    // TODO rewrite moduleparser and statementparser
    private static Identifier currentModPath() {
        return new Identifier(lastModTreeSpan(), modTree.toArray(new String[0]));
    }

    private static final List<String> modTree = new ArrayList<>();
    private static final List<Span> lastSpans = new ArrayList<>();

    private static Span lastModTreeSpan() {
        return lastSpans.get(lastSpans.size() - 1);
    }

    // TODO: not public unless un-inline global = true
    private static Identifier makeId(Span span, List<String> name) {
        String[] strs = new String[modTree.size() + name.size()];
        for (int i = 0; i < modTree.size(); i++) {
            strs[i] = modTree.get(i);
        }
        for (int i = 0; i < name.size(); i++) {
            strs[i + modTree.size()] = name.get(i);
        }
        return new Identifier(span, strs);
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
        Span nameSpan = scanner.spanAtRel(-1);
        if (llvmName == null && extern) { // if extern and unspecified name
            llvmName = name;
        }
        scanner.next().mustBe(Token.Punctuation.L_PAREN);
        List<Identifier> paramNames = new ArrayList<>();
        List<FOType> paramTypes = new ArrayList<>();
        while (!scanner.peek().is(Token.Punctuation.R_PAREN)) {
            if (paramTypes.size() > 0) scanner.next().mustBe(Token.Punctuation.COMMA);
            String paramName = scanner.next().identifier();
            Span paramNameSpan = scanner.spanAtRel(-1);
            scanner.next().mustBe(Token.Punctuation.COLON);
            FOType type = CommonParser.TYPE.parse(scanner);
            paramNames.add(new Identifier(paramNameSpan, paramName));
            paramTypes.add(type);
        }
        scanner.next();
        FOType result;
        if (scanner.peek().is(Token.Punctuation.ARROW)) {
            scanner.next();
            result = CommonParser.TYPE.parse(scanner);
        } else {
            result = FOType.VOID;
        }
        if(result == null) throw new SourceCodeException("expected return type", scanner.spanAtRel(-1));
        FunctionType funcType = new FunctionType(result, paramTypes);
        Block body;
        if (extern) {
            body = null;
        } else {
            body = StatementParser.forceBlock(scanner);
        }
        if (scanner.hasNext(Token.Punctuation.SEMICOLON)) scanner.next();
        // this identifier is global because its the true canonical identifier, not local
        // same for the attemptStruct makeId call
        return new Function(makeId(nameSpan, List.of(name)), inline, funcType, paramNames, body, llvmName, false);
    }

    public static StructType attemptStruct(TokenScanner scanner) {
        TokenScanner s = scanner.copy();
//        boolean packed = s.peek().is(Token.Keyword.PACKED);
//        if (packed) s.next();
        if (!s.peek().is(Token.Keyword.STRUCT)) return null;
        s.next();
        if (!s.peek().is(Token.Type.IDENTIFIER)) return null;
        String name = s.next().identifier();
        Span nameSpan = s.spanAtRel(-1);
        List<String> fieldNames = new ArrayList<>();
        List<FOType> fieldTypes = new ArrayList<>();

        if (s.peek().is(Token.Punctuation.L_BRACKET)) {
            scanner.index = s.index;
            scanner.next().mustBe(Token.Punctuation.L_BRACKET);
            while (!scanner.peek().is(Token.Punctuation.R_BRACKET)) {
                fieldTypes.add(CommonParser.TYPE.parse(scanner));
                fieldNames.add(scanner.next().identifier());
                scanner.next().mustBe(Token.Punctuation.SEMICOLON);
            }
            scanner.next();
            if (scanner.hasNext(Token.Punctuation.SEMICOLON)) scanner.next();
            return new StructType(makeId(nameSpan, List.of(name)), fieldNames, fieldTypes);
        } else {
            return null;
        }
    }

    private static GlobalVariable attemptGlobal(TokenScanner scanner) {
        TokenScanner s = scanner.copy();
        FOType type = CommonParser.TYPE.parse(s);
        if (type == null) return null;
        if (s.hasNext(Token.Type.IDENTIFIER)) {
            String name = s.next().identifier();
            Span nameSpan = s.spanAtRel(-1);
            if (s.hasNext(Token.Punctuation.EQ)) {
                s.next();
                scanner.index = s.index;
                Expression value = ExpressionParser.EXPR.parse(scanner);
                scanner.next().mustBe(Token.Punctuation.SEMICOLON);
                return new GlobalVariable(type, makeId(nameSpan, List.of(name)), value);
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

    public static UnresolvedType attemptTypeReference(TokenScanner scanner) {
        if (!scanner.hasNext(Token.Type.IDENTIFIER) && !scanner.hasNext(Token.Punctuation.DOUBLE_COLON)) return null;
        Span start = scanner.spanAtRel(0);
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
        Span end = scanner.spanAtRel(-1);
        Span span = Span.concat(start, end);
        if (global) {
            return new UnresolvedType(new Identifier(span, id));
        } else {
            return new UnresolvedType(makeId(Span.concat(start, end), id));
        }
    }

}
