package mackycheese21.ferricoxide.token;

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

    public char peek() throws TokenException {
        if (index == data.length()) {
            throw new TokenException(TokenException.Type.UNEXPECTED_EOF, new Span(data.length() - 1, data.length()));
        }
        return data.charAt(index);
    }

    public boolean hasNext(String valid) {
        try {
            return hasNext() && valid.contains("" + peek());
        } catch (TokenException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasNextNot(String bad) {
        try {
            return hasNext() && !bad.contains("" + peek());
        } catch (TokenException e) {
            throw new RuntimeException(e);
        }
    }

    public char next() throws TokenException {
        if (index == data.length()) {
            throw new TokenException(TokenException.Type.UNEXPECTED_EOF, new Span(data.length() - 1, data.length()));
        }
        return data.charAt(index++);
    }

    public CodeScanner copy() {
        CodeScanner codeScanner = new CodeScanner(data);
        codeScanner.index = index;
        return codeScanner;
    }
}
