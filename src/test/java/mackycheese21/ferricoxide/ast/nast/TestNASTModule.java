package mackycheese21.ferricoxide.ast.nast;

import mackycheese21.ferricoxide.ast.module.FOModule;
import mackycheese21.ferricoxide.ast.module.Function;
import mackycheese21.ferricoxide.parser.ModuleParser;
import mackycheese21.ferricoxide.ast.visitor.StringifyVisitor;
import mackycheese21.ferricoxide.parser.token.TokenScanner;
import mackycheese21.ferricoxide.parser.token.Tokenizer;

public class TestNASTModule {

    public static void main(String[] args) {
        String data = "inline i32 foo(i32 x, i32 y) { i32 z = x + y; return z * x; } extern void bar(void z);";
        FOModule module = ModuleParser.parse(new TokenScanner(Tokenizer.tokenize(data)));
        for (Function function : module.functions) {
            System.out.println("---");
            System.out.println(function.inline);
            System.out.println(function.isExtern());
            System.out.println(function.name);
            System.out.println(function.type);
            System.out.println(function.paramNames);
            if (function.body != null) {
                System.out.println(function.body.visit(new StringifyVisitor("\t")));
            } else {
                System.out.println("extern");
            }
        }
    }

}
