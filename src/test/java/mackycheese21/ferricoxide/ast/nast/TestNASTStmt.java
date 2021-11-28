package mackycheese21.ferricoxide.ast.nast;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.ast.ConcreteType;
import mackycheese21.ferricoxide.ast.IdentifierMap;
import mackycheese21.ferricoxide.SourceCodeException;
import mackycheese21.ferricoxide.parser.StatementParser;
import mackycheese21.ferricoxide.ast.stmt.Statement;
import mackycheese21.ferricoxide.ast.visitor.StringifyVisitor;
import mackycheese21.ferricoxide.ast.visitor.TypeValidatorVisitor;
import mackycheese21.ferricoxide.parser.token.Token;
import mackycheese21.ferricoxide.parser.token.TokenScanner;
import mackycheese21.ferricoxide.parser.token.Tokenizer;

import java.util.List;

public class TestNASTStmt {

    private static IdentifierMap<ConcreteType> vars = new IdentifierMap<>(null);

    private static void run(String s) {
        List<Token> tokens = Tokenizer.tokenize(s);
        System.out.println("\n\n-------");
        System.out.println(s);
        Statement statement;
        try {
            statement = StatementParser.forceStatement(new TokenScanner(tokens));
        } catch (SourceCodeException sce) {
            for (int i = 0; i < sce.span.start; i++) System.out.print(" ");
            for (int i = 0; i < sce.span.end - sce.span.start; i++) System.out.print("^");
            System.out.println();
            System.out.println(sce.getMessage());
            sce.printStackTrace();
            return;
        }
        System.out.println(statement.visit(new StringifyVisitor("\t")));
//        System.out.println(statement.visit(new VerboseStringifyVisitor()));
        IdentifierMap<ConcreteType.Function> functions = new IdentifierMap<>(null);
        functions.mapAdd("foo", new ConcreteType.Function(ConcreteType.I32, List.of(ConcreteType.I32, ConcreteType.I32)));
        try {
            statement.visit(new TypeValidatorVisitor(vars, functions));
            System.out.println("Validated!");
        } catch (AnalysisException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        run("i32 y = 5;");
        run("i32 x= foo(3,y);");
        run("while x > y { x = x + 1; }");
        run("if x > y { foo(2, y); } else { foo(x, 4); }");
        run("if x > y { foo(2, y); }");
        run("y = x * ( y+x) / y - y;");
    }

}
