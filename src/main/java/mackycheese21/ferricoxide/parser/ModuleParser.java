package mackycheese21.ferricoxide.parser;

import mackycheese21.ferricoxide.SourceCodeException;
import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.hl.mod.FunctionDef;
import mackycheese21.ferricoxide.ast.hl.type.HLType;
import mackycheese21.ferricoxide.parser.token.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ModuleParser {

    private static final List<String> modTree = new ArrayList<>();

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

//    private static FunctionDef forceFunction(TokenScanner scanner, boolean inImpl) {
//        boolean inline = scanner.peek() instanceof IdentToken ident && ident.value.equals("inline");
//        if (inline) scanner.next();
//
//        boolean extern = scanner.peek() instanceof IdentToken ident && ident.value.equals("extern");
//        String llvmName = null;
//        if (extern) {
//            scanner.next();
//            if (scanner.peek() instanceof GroupToken group && group.type == GroupToken.Type.PAREN) {
//                scanner.next();
//                TokenScanner externScanner = new TokenScanner(group.value);
//                llvmName = externScanner.next().requireStringLiteral().value;
//                externScanner.requireEmpty();
//            }
//        }
//
//        boolean export = scanner.peek() instanceof IdentToken ident && ident.value.equals("export");
//        if (extern && export) throw new RuntimeException();
//        if (export) {
//            scanner.next();
//            if (scanner.peek() instanceof GroupToken group && group.type == GroupToken.Type.PAREN) {
//                scanner.next();
//                TokenScanner exportScanner = new TokenScanner(group.value);
//                llvmName = exportScanner.next().requireStringLiteral().value;
//                exportScanner.requireEmpty();
//            }
//        }
//
//        scanner.next().requireIdent().requireValue("fn");
//
//        String name = scanner.next().requireIdent().value;
//        Span nameSpan = scanner.lastConsumedSpan();
//        if (llvmName == null && extern) { // if extern and unspecified name
//            llvmName = name;
//        }
//        TokenScanner paramScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.PAREN).value);
//        List<Identifier> paramNames = new ArrayList<>();
//        List<HLType> paramTypes = new ArrayList<>();
//        boolean staticMethod = false;
//        if (inImpl) { // inImpl always has a modTree entry
//            HLType selfType = enclosingType;
//            if (paramScanner.remaining() > 0 && paramScanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.AND) {
//                paramScanner.next();
//                selfType = new PointerType(selfType);
//            }
//            if (paramScanner.remaining() > 0 && paramScanner.peek() instanceof IdentToken ident && ident.value.equals("self")) {
//                paramScanner.next();
//                paramNames.add(new Identifier(ident.span(), "self"));
//                paramTypes.add(selfType);
//            } else {
//                staticMethod = true;
//            }
//        }
//        while (paramScanner.remaining() > 0) {
//            if (paramTypes.size() > 0) paramScanner.next().requirePunct(PunctToken.Type.COMMA);
//            String paramName = paramScanner.next().requireIdent().value;
//            Span paramNameSpan = paramScanner.lastConsumedSpan();
//            paramScanner.next().requirePunct(PunctToken.Type.COLON);
//            FOType type = CommonParser.forceType(paramScanner);
//            paramNames.add(new Identifier(paramNameSpan, paramName));
//            paramTypes.add(type);
//        }
//        paramScanner.requireEmpty();
//        FOType result;
//        if (scanner.remaining() > 0 && scanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.R_ARROW) {
//            scanner.next();
//            result = CommonParser.forceType(scanner);
//        } else {
//            result = FOType.VOID;
//        }
//        if (result == null) throw new SourceCodeException(scanner.lastConsumedSpan(), "expected return type");
//        FunctionType funcType = new FunctionType(result, paramTypes);
//        Block body;
//        if (extern) {
//            body = null;
//        } else {
//            body = StatementParser.forceBlock(scanner);
//        }
//        output += currentIndent;
//        if (inline) output += "inline ";
//        if (extern) {
//            if (llvmName.equals(name)) {
//                output += "extern ";
//            } else {
//                output += "extern(\"%s\") ".formatted(llvmName);
//            }
//        }
//        if (export) output += "export(\"%s\") ".formatted(llvmName);
//        output += "fn %s(%s)%s".formatted(name, IntStream.range(0, paramNames.size()).mapToObj(i -> "%s: %s".formatted(paramNames.get(i), paramTypes.get(i))).collect(Collectors.joining(", ")), result == FOType.VOID ? "" : " -> %s".formatted(result.toString()));
//        if (extern) {
//            output += "\n";
//        } else {
//            output += " {\n";
//            push();
//            for (Statement stmt : body.statements) {
//                StringifyStatementVisitor ssv = new StringifyStatementVisitor(currentIndent);
//                ssv.currentIndent = currentIndent;
//                stmt.visit(ssv);
//                output += ssv.getOutput();
//            }
//            pop();
//            output += "%s}\n".formatted(currentIndent);
//        }
//        Identifier finalFuncId;
//        if (enclosingType != null) {
//            finalFuncId = makeId(nameSpan, List.of(enclosingType.toString(), name));
//        } else {
//            finalFuncId = makeId(nameSpan, List.of(name));
//        }
//        boolean modRefUpOne = enclosingType != null;
//        if (staticMethod) {
//            // TODO enclosingType.toString is super hacky, make it so you cant impl tuples, only things with identifier names
//            return new Function(finalFuncId, inline, funcType, paramNames, body, llvmName, false, null, modRefUpOne);
//        }
//        return new Function(finalFuncId, inline, funcType, paramNames, body, llvmName, false, enclosingType, modRefUpOne);
//    }

//    public static StructType attemptStruct(TokenScanner scanner) {
//        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("struct")) {
//            scanner.next();
//            String name = scanner.next().requireIdent().value;
//            Span nameSpan = scanner.lastConsumedSpan();
//            List<String> fieldNames = new ArrayList<>();
//            List<FOType> fieldTypes = new ArrayList<>();
//
//            TokenScanner fieldScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.CURLY_BRACKET).value);
//            while (fieldScanner.remaining() > 0) {
//                if (fieldTypes.size() > 0) {
//                    fieldScanner.next().requirePunct(PunctToken.Type.COMMA);
//                }
//                fieldNames.add(fieldScanner.next().requireIdent().value);
//                fieldScanner.next().requirePunct(PunctToken.Type.COLON);
//                fieldTypes.add(CommonParser.forceType(fieldScanner));
//            }
//            fieldScanner.requireEmpty();
//
//            output += "%sstruct %s {\n".formatted(currentIndent, name);
//            push();
//            for (int i = 0; i < fieldNames.size(); i++) {
//                output += "%s%s: %s".formatted(currentIndent, fieldNames.get(i), fieldTypes.get(i).longName);
//                if (i == fieldNames.size() - 1) {
//                    output += "\n";
//                } else {
//                    output += ",\n";
//                }
//            }
//            pop();
//            output += "%s}\n".formatted(currentIndent);
//
//            return new StructType(makeId(nameSpan, List.of(name)), fieldNames, fieldTypes);
//        }
//        return null;
//    }
//
//    private static GlobalVariable attemptGlobal(TokenScanner scanner) {
//        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("let")) {
//            scanner.next();
//            String name = scanner.next().requireIdent().value;
//            Span nameSpan = scanner.lastConsumedSpan();
//            scanner.next().requirePunct(PunctToken.Type.COLON);
//            FOType type = CommonParser.forceType(scanner);
//            scanner.next().requirePunct(PunctToken.Type.EQ);
//            Expression value = ExpressionParser.forceExpr(scanner);
////            scanner.next().requirePunct(PunctToken.Type.SEMICOLON);
//            output += "%slet %s: %s = %s\n".formatted(currentIndent, name, type.longName, value.stringify());
//            return new GlobalVariable(type, makeId(nameSpan, List.of(name)), value);
//        }
//        return null;
//    }
//
//    private static boolean attemptMod(TokenScanner scanner, FOModule module, boolean inImpl) {
//        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("mod")) {
//            if (inImpl) throw new SourceCodeException(ident.span(), "no mod inside impl");
//            scanner.next();
//            String name = scanner.next().requireIdent().value;
//
//            modTree.add(name);
//            output += "%smod %s {\n".formatted(currentIndent, name);
//
//            push();
//            TokenScanner innerScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.CURLY_BRACKET).value);
//            parseInto(innerScanner, module, inImpl);
//            pop();
//            output += "%s}\n".formatted(currentIndent);
//            innerScanner.requireEmpty();
//            modTree.remove(modTree.size() - 1);
//
//            return true;
//        }
//        return false;
//    }
//
//    private static List<Function> implTrait(TokenScanner scanner) {
//        List<Function> functions = new ArrayList<>();
//        while(scanner.remaining() > 0) {
//            functions.add(forceFunction(scanner, true));
//        }
//        scanner.requireEmpty();
//        return functions;
//    }
//
//    private static boolean attemptImpl(TokenScanner scanner, FOModule module, boolean inImpl) {
//        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("impl")) {
//            if (inImpl) throw new SourceCodeException(ident.span(), "no impl inside impl");
//            scanner.next();
//            Identifier name1 = CommonParser.forceIdentifier(scanner);
//            if(scanner.peek() instanceof IdentToken forIdent && forIdent.value.equals("for")) {
//                scanner.next();
//                Identifier traitName = name1;
//                Identifier structName = CommonParser.forceIdentifier(scanner);
//                enclosingType = new UnresolvedType(structName);
//                List<Function> functions = implTrait(new TokenScanner(scanner.next().requireGroup(GroupToken.Type.CURLY_BRACKET).value));
//                module.impls.add(new TraitImpl(structName, traitName, functions));
//                return true;
//            }
//            Identifier structName = name1;
//            enclosingType = new UnresolvedType(structName);
//            List<Function> functions = implTrait(new TokenScanner(scanner.next().requireGroup(GroupToken.Type.CURLY_BRACKET).value));
//            module.functions.addAll(functions); // TODO this disgusts me more than transphobia, both because of the stupidity and because of the extreme lack of planning that caused it
//            // like fr all these problems could be avoided if i sat down with a uml diagram
//            // hmm
//            // fukit im going to just go do stuff
//            return true;
//        }
//        return false;
//    }
//
//    private static FOType enclosingType = null;
//
//    private static void parseInto(TokenScanner scanner, FOModule module, boolean inImpl) {
//        // (implStructName == null) the lack of a struct to implement
//        // if the state of being in an impl is equivalent to the lack of a struct to implement
//        // :b:ad
//        if (inImpl == (enclosingType == null)) throw new UnsupportedOperationException();
//        while (scanner.remaining() > 0) {
//            if (attemptMod(scanner, module, inImpl)) {
//                continue;
//            }
//            if (attemptImpl(scanner, module, inImpl)) {
//                continue;
//            }
//
//            GlobalVariable global = attemptGlobal(scanner);
//            if (global != null) {
//                module.globals.add(global);
//                continue;
//            }
//
//            Trait trait = attemptTrait(scanner);
//            if (trait != null) {
//                module.traits.add(trait);
//                continue;
//            }
//
//            StructType struct = attemptStruct(scanner);
//            if (struct != null) {
//                module.structs.add(struct);
//                continue;
//            }
//
//            module.functions.add(forceFunction(scanner, inImpl));
//        }
//    }
//
//    private static Trait attemptTrait(TokenScanner scanner) {
//        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("trait")) {
//            scanner.next();
//            String name = scanner.next().requireIdent().value;
//            Span nameSpan = scanner.lastConsumedSpan();
//            TokenScanner methodScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.CURLY_BRACKET).value);
//            List<FunctionPrototype> methods = new ArrayList<>();
//            while (methodScanner.remaining() > 0) {
//                methodScanner.next().requireIdent().requireValue("fn");
//                String funcName = methodScanner.next().requireIdent().value;
//                Span funcNameSpan = methodScanner.lastConsumedSpan();
//                TokenScanner paramScanner = new TokenScanner(methodScanner.next().requireGroup(GroupToken.Type.PAREN).value);
//                List<String> paramNames = new ArrayList<>();
//                List<FOType> paramTypes = new ArrayList<>();
//                while (paramScanner.remaining() > 0) {
//                    if (paramNames.size() > 0) paramScanner.next().requirePunct(PunctToken.Type.COMMA);
//                    paramNames.add(paramScanner.next().requireIdent().value);
//                    paramScanner.next().requirePunct(PunctToken.Type.COLON);
//                    paramTypes.add(CommonParser.forceType(paramScanner));
//                }
//                paramScanner.requireEmpty();
//                FOType result = FOType.VOID;
//                if (methodScanner.remaining() > 0 && methodScanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.R_ARROW) {
//                    methodScanner.next();
//                    result = CommonParser.forceType(methodScanner);
//                }
//                methods.add(new FunctionPrototype(new Identifier(funcNameSpan, funcName), paramTypes, paramNames, result));
//                // TODO default methods for traits HOLY SHIT FUCK THIS CODE IS COMPLICATED
//            }
//            methodScanner.requireEmpty();
//            // TODO parse parent trait, I just want to get super-basic polymorphism working
//            return new Trait(new Identifier(nameSpan, name), null, methods);
//        }
//        return null;
//    }

//    public static FOModule parse(TokenScanner scanner) {
//        List<GlobalVariable> globals = new ArrayList<>();
//        List<StructType> structs = new ArrayList<>();
//        List<Function> functions = new ArrayList<>();
//        List<Trait> traits = new ArrayList<>();
//        List<TraitImpl> impls = new ArrayList<>();
//        FOModule module = new FOModule(globals, structs, functions, traits, impls, "");
//        output = "";
//        parseInto(scanner, module, false);
//        module.formatted = output;
//        return module;
//    }

}
