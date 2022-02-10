package mackycheese21.ferricoxide.parser;

import mackycheese21.ferricoxide.Identifier;
import mackycheese21.ferricoxide.Pair;
import mackycheese21.ferricoxide.nast.hl.HLModule;
import mackycheese21.ferricoxide.nast.hl.def.*;
import mackycheese21.ferricoxide.nast.hl.expr.HLBlock;
import mackycheese21.ferricoxide.nast.hl.expr.HLDiscard;
import mackycheese21.ferricoxide.nast.hl.expr.HLExpression;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;
import mackycheese21.ferricoxide.parser.token.*;

import java.util.ArrayList;
import java.util.List;

public class ModuleParser {
    // TODO: modPath isn't added to identifiers or anything, thats done by HLModuleCompiler

    private ModuleParser() {

    }

    public static HLModule parse(TokenScanner scanner) {
        HLModule module = new HLModule();
        new ModuleParser().parseInto(scanner, module);
        return module;
    }

    private final List<String> modTree = new ArrayList<>();

    private Identifier inMod(IdentToken ident) {
        return Identifier.concat(new Identifier(null, modTree), new Identifier(ident.span(), ident.value));
    }

    private void parseInto(TokenScanner scanner, HLModule module) {
        while (scanner.remaining() > 0) {
            parseItem(scanner, module);
        }
        scanner.requireEmpty();
    }

    private void parseItem(TokenScanner scanner, HLModule module) {
        /*
        -- macros like include! or macro_rules! are already processed by this point
        impl => SCE + to do
        trait => SCE + to do
        mod => force mod
        let => force global
        anything else => force fn
         */
        String token = scanner.peek().requireIdent().value;
        switch (token) {
            case "impl" -> forceImpl(scanner, module);
//            case "trait" -> forceTrait(scanner, module);
            case "mod" -> forceMod(scanner, module);
            case "let" -> forceLet(scanner, module);
            case "struct" -> forceStruct(scanner, module);
            default -> forceFn(scanner, module);
        }
    }

    private HLImplFunctionPrototype forceImplFunctionPrototype(Identifier modPath, HLTypeId enclosingType, TokenScanner scanner) {
        boolean sttc = scanner.peek() instanceof IdentToken ident && ident.value.equals("static");
        if (sttc) scanner.next();
        scanner.next().requireIdent().requireValue("fn");
        IdentToken name = scanner.next().requireIdent();
        List<Pair<String, HLTypeId>> params = new ArrayList<>();
        TokenScanner paramScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.PAREN).value);
        while (paramScanner.remaining() > 0) {
            if (params.size() > 0) paramScanner.next().requirePunct(PunctToken.Type.COMMA);
            String paramName = paramScanner.next().requireIdent().value;
            paramScanner.next().requirePunct(PunctToken.Type.COLON);
            HLTypeId paramType = CommonParser.forceType(paramScanner);
            params.add(new Pair<>(paramName, paramType));
        }
        paramScanner.requireEmpty();
        HLTypeId result = HLTypeId.none(name.span());
        if (scanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.R_ARROW) {
            scanner.next();
            result = CommonParser.forceType(scanner);
        }
        return new HLImplFunctionPrototype(sttc, enclosingType, new Identifier(name), modPath, params, result);
    }

    private HLImplFunctionDef forceImplFunctionDef(HLTypeId enclosingType, TokenScanner scanner) {
        HLImplFunctionPrototype proto = forceImplFunctionPrototype(new Identifier(Span.NONE, modTree), enclosingType, scanner);
        HLExpression body = ExpressionParser.forceBlock(scanner);
        return new HLImplFunctionDef(proto, body);
    }

    private void forceImpl(TokenScanner scanner, HLModule module) {
        Span start = scanner.next().span();
        HLTypeId enclosingType = CommonParser.forceType(scanner);
        TokenScanner implScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.CURLY_BRACKET).value);
        List<HLImplFunctionDef> functions = new ArrayList<>();
        while (implScanner.remaining() > 0) {
            functions.add(forceImplFunctionDef(enclosingType, implScanner));
        }
        implScanner.requireEmpty();
        module.impls.add(new HLImplDef(Span.concat(start, scanner.lastConsumedSpan()), new Identifier(Span.NONE, modTree), enclosingType, functions));
    }

    private void forceMod(TokenScanner scanner, HLModule module) {
        scanner.next();
        IdentToken modName = scanner.next().requireIdent();
        modTree.add(modName.value);
        parseInto(scanner, module);
        modTree.remove(modTree.size() - 1);
    }

    private void forceLet(TokenScanner scanner, HLModule module) {
        Span start = scanner.next().span();
        IdentToken name = scanner.next().requireIdent();
        scanner.next().requirePunct(PunctToken.Type.COLON);
        HLTypeId type = CommonParser.forceType(scanner);
        scanner.next().requirePunct(PunctToken.Type.EQ);
        HLExpression value = ExpressionParser.forceExpr(scanner);
        if (!(value instanceof HLDiscard)) scanner.next().requirePunct(PunctToken.Type.SEMICOLON);
        // TODO global span
        module.globals.add(new HLGlobalDef(Span.concat(start, scanner.lastConsumedSpan()), inMod(name), type, value));
    }

    private void forceStruct(TokenScanner scanner, HLModule module) {
        Span start = scanner.next().span();
        IdentToken name = scanner.next().requireIdent();
        TokenScanner fieldScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.CURLY_BRACKET).value);
        List<Pair<String, HLTypeId>> fields = new ArrayList<>();
        while (true) {
            if (fieldScanner.remaining() == 0) break;
            if (fields.size() > 0) {
                fieldScanner.next().requirePunct(PunctToken.Type.COMMA);
            }
            if (fieldScanner.remaining() == 0 || !(fieldScanner.peek() instanceof IdentToken)) break;
            String fieldName = fieldScanner.next().requireIdent().value;
            fieldScanner.next().requirePunct(PunctToken.Type.COLON);
            HLTypeId fieldType = CommonParser.forceType(fieldScanner);
            fields.add(new Pair<>(fieldName, fieldType));
        }
        fieldScanner.consumeCommaIfPresent();
        fieldScanner.requireEmpty();

        // optional semicolon after struct
        if (scanner.remaining() > 0 && scanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.SEMICOLON)
            scanner.next();
        // TODO struct span
        module.structs.add(new HLStructDef(Span.concat(start, scanner.lastConsumedSpan()), inMod(name), fields));
    }

    private void forceFn(TokenScanner scanner, HLModule module) {
        Span start = scanner.peek().span();

        boolean extern = false;
        boolean export = false;
        String llvmName = null;

        if (scanner.peek() instanceof IdentToken ident && ident.value.equals("extern")) {
            extern = true;
            scanner.next();
            if (scanner.peek() instanceof GroupToken group && group.type == GroupToken.Type.PAREN) {
                scanner.next();
                TokenScanner llvmNameScanner = new TokenScanner(group.value);
                llvmName = llvmNameScanner.next().requireStringLiteral().value;
                llvmNameScanner.requireEmpty();
            }
        } else if (scanner.peek() instanceof IdentToken ident && ident.value.equals("export")) {
            export = true;
            scanner.next();
            if (scanner.peek() instanceof GroupToken group && group.type == GroupToken.Type.PAREN) {
                scanner.next();
                TokenScanner llvmNameScanner = new TokenScanner(group.value);
                llvmName = llvmNameScanner.next().requireStringLiteral().value;
                llvmNameScanner.requireEmpty();
            }
        }

        if (!extern && !export) llvmName = null;

        scanner.next().requireIdent().requireValue("fn");
        IdentToken name = scanner.next().requireIdent();
        // TODO &self / self
        TokenScanner paramScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.PAREN).value);
        List<Pair<String, HLTypeId>> params = new ArrayList<>();
        while (paramScanner.remaining() > 0) {
            if (params.size() > 0) paramScanner.next().requirePunct(PunctToken.Type.COMMA);
            String paramName = paramScanner.next().requireIdent().value;
            paramScanner.next().requirePunct(PunctToken.Type.COLON);
            HLTypeId paramType = CommonParser.forceType(paramScanner);
            params.add(new Pair<>(paramName, paramType));
        }
        paramScanner.requireEmpty();
        HLTypeId result = HLTypeId.none(name.span());
        if (scanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.R_ARROW) {
            scanner.next();//TODO void result span
            result = CommonParser.forceType(scanner);
        }


        HLBlock body = null;
        if (extern) {
            scanner.next().requirePunct(PunctToken.Type.SEMICOLON);
        } else {
            body = ExpressionParser.forceBlock(scanner);
        }
        //TODO function span
        module.functions.add(new HLFunctionDef(Span.concat(start, scanner.lastConsumedSpan()), inMod(name), llvmName == null ? inMod(name).toLLVMString() : llvmName, params, result, body, llvmName != null));
    }

}
