package mackycheese21.ferricoxide.ast.token;

import mackycheese21.ferricoxide.ast.AstOpt;
import mackycheese21.ferricoxide.ast.SourceCodeException;

public class CodeScanner {
    public int index;
    private final String data;

    public CodeScanner(String data) {
        index = 0;
        this.data = data;
    }

    public boolean hasNext() {
        return index < data.length();
    }

    public AstOpt<Character> peek() {
        if (index == data.length()) {
            return AstOpt.error(new SourceCodeException(SourceCodeException.Type.UNEXPECTED_EOF, new Span(data.length() - 1, data.length())));
        }
        return AstOpt.value(data.charAt(index));
    }

    public boolean hasNext(String valid) {
        return hasNext() && valid.contains("" + peek().unwrapUnsafe());
    }

    public boolean hasNextNot(String bad) {
        return hasNext() && !bad.contains("" + peek().unwrapUnsafe());
    }

    public AstOpt<Character> next() {
        if (index == data.length()) {
            return AstOpt.error(new SourceCodeException(SourceCodeException.Type.UNEXPECTED_EOF, new Span(data.length() - 1, data.length())));
        }
        return AstOpt.value(data.charAt(index++));
    }

    public CodeScanner copy() {
        CodeScanner codeScanner = new CodeScanner(data);
        codeScanner.index = index;
        return codeScanner;
    }
}
