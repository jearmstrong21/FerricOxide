package mackycheese21.ferricoxide.token;

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

    public Token peek() throws TokenException {
        if(index == data.size()) {
            throw new TokenException(TokenException.Type.UNEXPECTED_EOF, data.get(data.size() - 1));
        }
        return data.get(index);
    }

    public boolean hasNext(Token.Type... types) {
        try {
            return hasNext() && peek().is(types);
        } catch (TokenException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasNext(Token.Punctuation... punctuations) {
        try {
            return hasNext(Token.Type.PUNCTUATION) && peek().is(punctuations);
        } catch (TokenException e) {
            throw new RuntimeException(e);
        }
    }

    public Token next() throws TokenException {
        if (!hasNext()) {
            throw new TokenException(TokenException.Type.UNEXPECTED_EOF, data.get(data.size() - 1));
        }
        return data.get(index++);
    }

    public TokenScanner copy() {
        TokenScanner s = new TokenScanner(data);
        s.index = index;
        return s;
    }

}
