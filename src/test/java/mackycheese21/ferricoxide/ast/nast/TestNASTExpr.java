package mackycheese21.ferricoxide.ast.nast;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.ast.ConcreteType;
import mackycheese21.ferricoxide.ast.IdentifierMap;
import mackycheese21.ferricoxide.ast.expr.Expression;
import mackycheese21.ferricoxide.parser.ExpressionParser;
import mackycheese21.ferricoxide.ast.visitor.StringifyVisitor;
import mackycheese21.ferricoxide.ast.visitor.TypeValidatorVisitor;
import mackycheese21.ferricoxide.ast.visitor.VerboseStringifyVisitor;
import mackycheese21.ferricoxide.parser.token.Token;
import mackycheese21.ferricoxide.parser.token.TokenScanner;
import mackycheese21.ferricoxide.parser.token.Tokenizer;

import java.util.List;

public class TestNASTExpr {

    private static void run(String s) {
        List<Token> tokens = Tokenizer.tokenize(s);
        Expression expression = ExpressionParser.parse(new TokenScanner(tokens));
        System.out.println("\n\n-------");
        System.out.println(s);
        System.out.println(expression.visit(new StringifyVisitor("\t")));
        System.out.println(expression.visit(new VerboseStringifyVisitor()));
        IdentifierMap<ConcreteType.Function> functions = new IdentifierMap<>(null);
        functions.mapAdd("foo", new ConcreteType.Function(ConcreteType.I32, List.of(ConcreteType.I32, ConcreteType.I32)));
        try {
            System.out.println(expression.visit(new TypeValidatorVisitor(new IdentifierMap<>(null), functions)));
            System.out.println("Validated!");
        } catch (AnalysisException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        run("3*5+7");
        run("3+5*7");
        run("3-5*-7");
        run("-5*3");
        run("if 5*2>3{true}else{5}");
        run("-(5*3)");
        run("5 * foo(3 + 5 * 7, 3 * 5 + 7)");
        run("5 * x + foo(3, 4)");
    }

}
