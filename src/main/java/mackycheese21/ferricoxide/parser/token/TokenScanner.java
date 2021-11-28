package mackycheese21.ferricoxide.parser.token;

import mackycheese21.ferricoxide.SourceCodeException;

import java.util.List;

public class TokenScanner {

    private final List<Token> data;
    public int index;

    public TokenScanner(List<Token> data) {
        this.data = data;
    }

    public boolean hasNext() {
        return index < data.size();
    }

    public Token currentOrLast() {
        if (hasNext()) return data.get(index);
        else return data.get(data.size() - 1);
    }

    public Token peek() {
        if (index == data.size()) {
            throw SourceCodeException.unexpectedEOF(data.get(data.size() - 1));
        }
        return data.get(index);
    }

    public boolean hasNext(Token.Type... types) {
        return hasNext() && peek().is(types);
    }

    public boolean hasNext(Token.Punctuation... punctuations) {
        return hasNext() && peek().is(punctuations);
    }

    public Token next() {
        if (!hasNext()) {
            throw SourceCodeException.unexpectedEOF(data.get(data.size() - 1));
        }
        return data.get(index++);
    }

    public TokenScanner copy() {
        TokenScanner s = new TokenScanner(data);
        s.index = index;
        return s;
    }

}
