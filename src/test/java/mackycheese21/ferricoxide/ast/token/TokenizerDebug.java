package mackycheese21.ferricoxide.ast.token;

import mackycheese21.ferricoxide.ast.SourceCodeException;

import java.util.List;

public class TokenizerDebug {

    private static void test(String data) {
        System.out.println(data);
        try {
            List<Token> tokens = Tokenizer.tokenize(data);
            for (Token t : tokens) {
                System.out.println(t);
            }
            System.out.println();
        } catch (SourceCodeException e) {
            for (int i = 0; i < e.span.start; i++) {
                System.out.print(" ");
            }
            for (int i = 0; i < e.span.end - e.span.start; i++) {
                System.out.print("^");
            }
            System.out.println();
            if (e.token == null) {
                System.out.println(e.type);
            } else {
                System.out.println(e.type + ": " + e.token);
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        test("5 + 4");
        test("int factorial(int n){if(n==1){return 1;}else{return n*factorial(n-1);}}");
        test("int factorial ( int n ) { if ( n == 1 ) { return 1 ; } else { return n * factorial ( n - 1 ) ; } }");
        test("factorially!");

    }

}
