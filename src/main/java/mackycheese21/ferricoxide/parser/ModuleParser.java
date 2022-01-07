package mackycheese21.ferricoxide.parser;

import mackycheese21.ferricoxide.SourceCodeException;
import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.expr.Expression;
import mackycheese21.ferricoxide.ast.module.FOModule;
import mackycheese21.ferricoxide.ast.module.Function;
import mackycheese21.ferricoxide.ast.module.GlobalVariable;
import mackycheese21.ferricoxide.ast.stmt.Block;
import mackycheese21.ferricoxide.ast.stmt.Statement;
import mackycheese21.ferricoxide.ast.type.FOType;
import mackycheese21.ferricoxide.ast.type.FunctionType;
import mackycheese21.ferricoxide.ast.type.PointerType;
import mackycheese21.ferricoxide.ast.type.StructType;
import mackycheese21.ferricoxide.ast.visitor.StringifyStatementVisitor;
import mackycheese21.ferricoxide.parser.token.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ModuleParser {

    private static final List<String> modTree = new ArrayList<>();

    private static String output = "";
    public static String currentIndent = "";
    private static final String indent = "\t";

    private static void push() {
        currentIndent += indent;
    }

    private static void pop() {
        currentIndent = currentIndent.substring(0, currentIndent.length() - indent.length());
    }

    private static StringifyStatementVisitor ssv() {
        return new StringifyStatementVisitor(indent);
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

    private static Function forceFunction(TokenScanner scanner, boolean inImpl) {
        boolean inline = scanner.peek() instanceof IdentToken ident && ident.value.equals("inline");
        if (inline) scanner.next();
//        boolean inline = false;


        boolean extern = scanner.peek() instanceof IdentToken ident && ident.value.equals("extern");
        String llvmName = null;
        if (extern) {
            scanner.next();
            if (scanner.peek() instanceof GroupToken group && group.type == GroupToken.Type.PAREN) {
                scanner.next();
                TokenScanner externScanner = new TokenScanner(group.value);
                llvmName = externScanner.next().requireStringLiteral().value;
                externScanner.requireEmpty();
            }
        }

//        boolean export = false;
        boolean export = scanner.peek() instanceof IdentToken ident && ident.value.equals("export");
        if (extern && export) throw new RuntimeException();
        if (export) {
            scanner.next();
            if (scanner.peek() instanceof GroupToken group && group.type == GroupToken.Type.PAREN) {
                scanner.next();
                TokenScanner exportScanner = new TokenScanner(group.value);
                llvmName = exportScanner.next().requireStringLiteral().value;
                exportScanner.requireEmpty();
            }
        }
        if (!scanner.next().requireIdent().value.equals("fn"))
            throw new SourceCodeException(scanner.lastConsumedSpan(), "expected fn");
        String name = scanner.next().requireIdent().value;
        Span nameSpan = scanner.lastConsumedSpan();
        if (llvmName == null && extern) { // if extern and unspecified name
            llvmName = name;
        }
        TokenScanner paramScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.PAREN).value);
        List<Identifier> paramNames = new ArrayList<>();
        List<FOType> paramTypes = new ArrayList<>();
        if (inImpl) { // inImpl always has a modTree entry
            FOType selfType = enclosingType;
            if (paramScanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.AND) {
                paramScanner.next();
                selfType = new PointerType(selfType);
            }
            IdentToken self = paramScanner.next().requireIdent();
            self.requireValue("self");
            paramNames.add(new Identifier(self.span(), self.value));
            paramTypes.add(selfType);
        }
        while (paramScanner.remaining() > 0) {
            if (paramTypes.size() > 0) paramScanner.next().requirePunct(PunctToken.Type.COMMA);
            String paramName = paramScanner.next().requireIdent().value;
            Span paramNameSpan = paramScanner.lastConsumedSpan();
            paramScanner.next().requirePunct(PunctToken.Type.COLON);
            FOType type = CommonParser.forceType(paramScanner);
            paramNames.add(new Identifier(paramNameSpan, paramName));
            paramTypes.add(type);
        }
        paramScanner.requireEmpty();
        FOType result;
        if (scanner.remaining() > 0 && scanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.R_ARROW) {
            scanner.next();
            result = CommonParser.forceType(scanner);
        } else {
            result = FOType.VOID;
        }
        if (result == null) throw new SourceCodeException(scanner.lastConsumedSpan(), "expected return type");
        FunctionType funcType = new FunctionType(result, paramTypes);
        Block body;
        if (extern) {
            body = null;
        } else {
            body = StatementParser.forceBlock(scanner);
        }
        output += currentIndent;
        if(inline) output += "inline ";
        if(extern) {
            if(llvmName.equals(name)) {
                output += "extern ";
            } else {
                output += "extern(\"%s\") ".formatted(llvmName);
            }
        }
        if(export) output += "export(\"%s\") ".formatted(llvmName);
        output += "fn %s(%s)%s".formatted(name, IntStream.range(0, paramNames.size()).mapToObj(i -> "%s: %s".formatted(paramNames.get(i), paramTypes.get(i))).collect(Collectors.joining(", ")), result == FOType.VOID ? "" : " -> %s".formatted(result.toString()));
        if (extern) {
            output += "\n";
        } else {
            output += " {\n";
            push();
            for (Statement stmt : body.statements) {
                StringifyStatementVisitor ssv = new StringifyStatementVisitor(currentIndent);
                ssv.currentIndent = currentIndent;
                stmt.visit(ssv);
                output += ssv.getOutput();
            }
            pop();
            output += "%s}\n".formatted(currentIndent);
        }
        return new Function(makeId(nameSpan, List.of(name)), inline, funcType, paramNames, body, llvmName, false, enclosingType);
    }

    public static StructType attemptStruct(TokenScanner scanner) {
        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("struct")) {
            scanner.next();
            String name = scanner.next().requireIdent().value;
            Span nameSpan = scanner.lastConsumedSpan();
            List<String> fieldNames = new ArrayList<>();
            List<FOType> fieldTypes = new ArrayList<>();

            TokenScanner fieldScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.CURLY_BRACKET).value);
            while (fieldScanner.remaining() > 0) {
                if (fieldTypes.size() > 0) {
                    fieldScanner.next().requirePunct(PunctToken.Type.COMMA);
                }
                fieldNames.add(fieldScanner.next().requireIdent().value);
                fieldScanner.next().requirePunct(PunctToken.Type.COLON);
                fieldTypes.add(CommonParser.forceType(fieldScanner));
            }
            fieldScanner.requireEmpty();

            output += "%sstruct %s {\n".formatted(currentIndent, name);
            push();
            for (int i = 0; i < fieldNames.size(); i++) {
                output += "%s%s: %s".formatted(currentIndent, fieldNames.get(i), fieldTypes.get(i).longName);
                if (i == fieldNames.size() - 1) {
                    output += "\n";
                } else {
                    output += ",\n";
                }
            }
            pop();
            output += "%s}\n".formatted(currentIndent);

            return new StructType(makeId(nameSpan, List.of(name)), fieldNames, fieldTypes);
        }
        return null;
    }

    private static GlobalVariable attemptGlobal(TokenScanner scanner) {
        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("let")) {
            scanner.next();
            String name = scanner.next().requireIdent().value;
            Span nameSpan = scanner.lastConsumedSpan();
            scanner.next().requirePunct(PunctToken.Type.COLON);
            FOType type = CommonParser.forceType(scanner);
            scanner.next().requirePunct(PunctToken.Type.EQ);
            Expression value = ExpressionParser.forceExpr(scanner);
//            scanner.next().requirePunct(PunctToken.Type.SEMICOLON);
            output += "%slet %s: %s = %s\n".formatted(currentIndent, name, type.longName, value.stringify());
            return new GlobalVariable(type, makeId(nameSpan, List.of(name)), value);
        }
        return null;
    }

    private static FOType enclosingType = null;

    private static void parseInto(TokenScanner scanner, FOModule module, boolean inImpl) {
        // (implStructName == null) the lack of a struct to implement
        // if the state of being in an impl is equivalent to the lack of a struct to implement
        // :b:ad
        if (inImpl == (enclosingType == null)) throw new UnsupportedOperationException();
        while (scanner.remaining() > 0) {
            if (scanner.peek() instanceof IdentToken ident && ident.value.equals("mod")) {
                if (inImpl) throw new SourceCodeException(ident.span(), "no mod inside impl");
                scanner.next();
                String name = scanner.next().requireIdent().value;

                modTree.add(name);
                output += "%smod %s {\n".formatted(currentIndent, name);

                push();
                TokenScanner innerScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.CURLY_BRACKET).value);
                parseInto(innerScanner, module, inImpl);
                pop();
                output += "%s}\n".formatted(currentIndent);
                innerScanner.requireEmpty();
                modTree.remove(modTree.size() - 1);

            } else if (scanner.peek() instanceof IdentToken ident && ident.value.equals("impl")) {
                if (inImpl) throw new SourceCodeException(ident.span(), "no impl inside impl");
                scanner.next();
                enclosingType = CommonParser.forceType(scanner); // TODO generics

                output += "%simpl %s {\n".formatted(currentIndent, enclosingType.longName);
                push();

//                modTree.add(name);
                TokenScanner innerScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.CURLY_BRACKET).value);
                parseInto(innerScanner, module, true);
                innerScanner.requireEmpty();
//                modTree.remove(modTree.size() - 1);

                pop();
                output += "%s}\n".formatted(currentIndent);

                enclosingType = null;

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

                module.functions.add(forceFunction(scanner, inImpl));
            }
        }
    }

    public static FOModule parse(TokenScanner scanner) {
        List<GlobalVariable> globals = new ArrayList<>();
        List<StructType> structs = new ArrayList<>();
        List<Function> functions = new ArrayList<>();
        FOModule module = new FOModule(globals, structs, functions, "");
        output = "";
        parseInto(scanner, module, false);
        module.formatted = output;
        return module;
    }

}
