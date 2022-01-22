package mackycheese21.ferricoxide.parser;

import mackycheese21.ferricoxide.Pair;
import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.hl.expr.HLExpression;
import mackycheese21.ferricoxide.ast.hl.mod.*;
import mackycheese21.ferricoxide.ast.hl.stmt.HLStatement;
import mackycheese21.ferricoxide.ast.hl.type.HLKeywordType;
import mackycheese21.ferricoxide.ast.hl.type.HLType;
import mackycheese21.ferricoxide.parser.token.GroupToken;
import mackycheese21.ferricoxide.parser.token.IdentToken;
import mackycheese21.ferricoxide.parser.token.PunctToken;
import mackycheese21.ferricoxide.parser.token.TokenScanner;

import java.util.ArrayList;
import java.util.List;

public class ModuleParser {
    // TODO: modPath isn't added to identifiers or anything, thats done by HLModuleCompiler

    private ModuleParser() {

    }

    public static List<ModItem> parse(TokenScanner scanner) {
        List<ModItem> items = new ArrayList<>();
        new ModuleParser().parseInto(scanner, items);
        return items;
    }

    private final List<String> modTree = new ArrayList<>();

    private Identifier inMod(IdentToken ident) {
        return Identifier.concat(new Identifier(null, modTree), new Identifier(ident.span(), ident.value));
    }

    private void parseInto(TokenScanner scanner, List<ModItem> items) {
        while (scanner.remaining() > 0) {
            parseItem(scanner, items);
        }
        scanner.requireEmpty();
    }

    private void parseItem(TokenScanner scanner, List<ModItem> items) {
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
            case "impl" -> forceImpl(scanner, items);
            case "trait" -> forceTrait(scanner, items);
            case "mod" -> forceMod(scanner, items);
            case "let" -> forceLet(scanner, items);
            case "struct" -> forceStruct(scanner, items);
            default -> forceFn(scanner, items);
        }
    }

    private void forceImpl(TokenScanner scanner, List<ModItem> items) {
        throw new UnsupportedOperationException();
    }

    private void forceTrait(TokenScanner scanner, List<ModItem> items) {
        throw new UnsupportedOperationException();
    }

    private void forceMod(TokenScanner scanner, List<ModItem> items) {
        scanner.next();
        IdentToken modName = scanner.next().requireIdent();
        modTree.add(modName.value);
        List<ModItem> innerItems = new ArrayList<>();
        parseInto(scanner, innerItems);
        modTree.remove(modTree.size() - 1);
        items.add(new ModDef(modName.span(), modName.value, innerItems));
    }

    private void forceLet(TokenScanner scanner, List<ModItem> items) {
        scanner.next();
        IdentToken name = scanner.next().requireIdent();
        scanner.next().requirePunct(PunctToken.Type.COLON);
        HLType type = CommonParser.forceType(scanner);
        scanner.next().requirePunct(PunctToken.Type.EQ);
        HLExpression value = ExpressionParser.forceExpr(scanner);
        scanner.next().requirePunct(PunctToken.Type.SEMICOLON);
        // TODO global span
        items.add(new GlobalDef(name.span(), inMod(name), type, value));
    }

    private void forceStruct(TokenScanner scanner, List<ModItem> items) {
        scanner.next();
        IdentToken name = scanner.next().requireIdent();
        TokenScanner fieldScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.CURLY_BRACKET).value);
        List<Pair<String, HLType>> fields = new ArrayList<>();
        while (true) {
            if(fieldScanner.remaining() == 0) break;
            if (fields.size() > 0) {
                fieldScanner.next().requirePunct(PunctToken.Type.COMMA);
            }
            if(!(fieldScanner.peek() instanceof IdentToken)) break;
            String fieldName = fieldScanner.next().requireIdent().value;
            fieldScanner.next().requirePunct(PunctToken.Type.COLON);
            HLType fieldType = CommonParser.forceType(fieldScanner);
            fields.add(new Pair<>(fieldName, fieldType));
        }
        fieldScanner.consumeCommaIfPresent();
        fieldScanner.requireEmpty();

        // optional semicolon after struct
        if (scanner.remaining() > 0 && scanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.SEMICOLON)
            scanner.next();
        // TODO struct span
        items.add(new StructDef(name.span(), inMod(name), fields));
    }

    private FunctionPrototype forcePrototype(TokenScanner scanner) {
        scanner.next().requireIdent().requireValue("fn");
        IdentToken name = scanner.next().requireIdent();
        // TODO &self / self
        TokenScanner paramScanner = new TokenScanner(scanner.next().requireGroup(GroupToken.Type.PAREN).value);
        List<Pair<String, HLType>> params = new ArrayList<>();
        while (paramScanner.remaining() > 0) {
            if (params.size() > 0) paramScanner.next().requirePunct(PunctToken.Type.COMMA);
            String paramName = paramScanner.next().requireIdent().value;
            paramScanner.next().requirePunct(PunctToken.Type.COLON);
            HLType paramType = CommonParser.forceType(paramScanner);
            params.add(new Pair<>(paramName, paramType));
        }
        paramScanner.requireEmpty();
        HLType result = new HLKeywordType(name.span(), HLKeywordType.Type.VOID);
        if (scanner.peek() instanceof PunctToken punct && punct.type == PunctToken.Type.R_ARROW) {
            scanner.next();//TODO void result span
            result = CommonParser.forceType(scanner);
        }
        return new FunctionPrototype(inMod(name), params, result);
    }

    private void forceFn(TokenScanner scanner, List<ModItem> items) {
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
        } else if(scanner.peek() instanceof IdentToken ident && ident.value.equals("export")) {
            export = true;
            scanner.next();
            if(scanner.peek() instanceof GroupToken group && group.type == GroupToken.Type.PAREN) {
                scanner.next();
                TokenScanner llvmNameScanner = new TokenScanner(group.value);
                llvmName = llvmNameScanner.next().requireStringLiteral().value;
                llvmNameScanner.requireEmpty();
            }
        }

        FunctionPrototype prototype = forcePrototype(scanner);

        List<HLStatement> body = null;
        if (extern) {
            scanner.next().requirePunct(PunctToken.Type.SEMICOLON);
        } else {
            body = StatementParser.forceBlock(scanner).statements;
        }
        //TODO function span
        items.add(new FunctionDef(prototype.name.span, prototype, false, export, extern, llvmName, body));
    }

}
