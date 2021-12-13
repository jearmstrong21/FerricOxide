package mackycheese21.ferricoxide.parser.token;

import mackycheese21.ferricoxide.SourceCodeException;
import mackycheese21.ferricoxide.ast.AstOpt;

import java.nio.file.Path;

public class CodeScanner {

    public static record Snapshot(int index, int lineNum, int posInLine, Path path, CodeScanner scanner) {
        public Span into() {
            return new Span(posInLine, lineNum, scanner.index - index, path);
        }
    }

    public Snapshot snapshot() {
        return new Snapshot(index, lineNum, posInLine, path, this);
    }

    private int index;
    private int lineNum;
    private int posInLine;
    private final Path path;
    private final String data;

    public CodeScanner(String data, Path path) {
        index = 0;
        lineNum = 1;
        posInLine = 0;
        this.data = data;
        this.path = path;
    }

    public void copyFrom(CodeScanner other) {
        this.index = other.index;
        this.lineNum = other.lineNum;
        this.posInLine = other.posInLine;
        if (!path.equals(other.path)) throw new RuntimeException("Cannot copy code scanner from different file");
    }

    public boolean hasNext() {
        return index < data.length();
    }

    public Span latestChar() {
        return new Span(posInLine, 1, lineNum, path);
    }

    public AstOpt<Character> peek() {
        if (index == data.length()) {
            return AstOpt.error(SourceCodeException.unexpectedEOF(latestChar()));
        }
        return AstOpt.value(data.charAt(index));
    }

    public boolean hasNext(String valid) {
        return hasNext() && valid.contains("" + peek().unwrapUnsafe());
    }

    public boolean hasNextSequence(String sequence) {
        return data.startsWith(sequence, index);
    }

    public boolean hasNextNot(String bad) {
        return hasNext() && !bad.contains("" + peek().unwrapUnsafe());
    }

    public AstOpt<Character> next() {
        if (index == data.length()) {
            return AstOpt.error(SourceCodeException.unexpectedEOF(latestChar()));
        }
        char c = data.charAt(index);

        index++;
        if (c == '\n') {
            lineNum++;
            posInLine = 0;
        } else {
            posInLine++;
        }

        return AstOpt.value(c);
    }

    public CodeScanner copy() {
        CodeScanner codeScanner = new CodeScanner(data, path);
        codeScanner.index = index;
        codeScanner.lineNum = lineNum;
        codeScanner.posInLine = posInLine;
        return codeScanner;
    }
}
