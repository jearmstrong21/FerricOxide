package mackycheese21.ferricoxide.parser;

import mackycheese21.ferricoxide.ast.ConcreteType;
import mackycheese21.ferricoxide.ast.module.FOModule;
import mackycheese21.ferricoxide.ast.module.Function;
import mackycheese21.ferricoxide.ast.stmt.Block;
import mackycheese21.ferricoxide.parser.token.Token;
import mackycheese21.ferricoxide.parser.token.TokenScanner;

import java.util.ArrayList;
import java.util.List;

public class ModuleParser {

    private static Function attemptFunction(TokenScanner scanner) {
        boolean inline = scanner.peek().is(Token.Keyword.INLINE);
        if (inline) scanner.next();
        boolean extern = scanner.peek().is(Token.Keyword.EXTERN);
        if (extern) scanner.next();
        ConcreteType result = StatementParser.forceType(scanner);
        String name = scanner.next().identifier();
        scanner.next().mustBe(Token.Punctuation.L_PAREN);
        List<ConcreteType> paramTypes = new ArrayList<>();
        List<String> paramNames = new ArrayList<>();
        while (true) {
            if (scanner.peek().is(Token.Punctuation.R_PAREN)) break;
            if (paramTypes.size() > 0) scanner.next().mustBe(Token.Punctuation.COMMA);
            ConcreteType type = StatementParser.forceType(scanner);
            String paramName = scanner.next().identifier();
            paramTypes.add(type);
            paramNames.add(paramName);
        }
        scanner.next();
        ConcreteType.Function funcType = new ConcreteType.Function(result, paramTypes);
        Block body;
        if (extern) {
            body = null;
        } else {
            body = StatementParser.forceBlock(scanner);
        }
        if (scanner.hasNext(Token.Punctuation.SEMICOLON)) scanner.next();
        return new Function(name, inline, funcType, paramNames, body);
    }

    public static FOModule parse(TokenScanner scanner) {
        List<Function> functions = new ArrayList<>();
        while (scanner.hasNext()) {
            functions.add(attemptFunction(scanner));
        }
        return new FOModule(functions);
    }
    // TODO redo module ast / structure, preprocessor mayb?

}
