package mackycheese21.ferricoxide.ast.token;

import mackycheese21.ferricoxide.ast.AstOpt;
import mackycheese21.ferricoxide.ast.SourceCodeException;

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

    public AstOpt<Token> peek() {
        if (index == data.size()) {
            return AstOpt.error(new SourceCodeException(SourceCodeException.Type.UNEXPECTED_EOF, data.get(data.size() - 1)));
        }
        return AstOpt.value(data.get(index));
    }

    public boolean hasNext(Token.Type... types) {
        return hasNext() && peek().unwrapUnsafe().is(types);
    }

    public boolean hasNext(Token.Punctuation... punctuations) {
        return hasNext(Token.Type.PUNCTUATION) && peek().unwrapUnsafe().is(punctuations);
    }

    public AstOpt<Token> next() {
        if (!hasNext()) {
            return AstOpt.error(new SourceCodeException(SourceCodeException.Type.UNEXPECTED_EOF, data.get(data.size() - 1)));
        }
        return AstOpt.value(data.get(index++));
    }

    public TokenScanner copy() {
        TokenScanner s = new TokenScanner(data);
        s.index = index;
        return s;
    }

}
