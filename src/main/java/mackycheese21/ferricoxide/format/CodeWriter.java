package mackycheese21.ferricoxide.format;

public class CodeWriter {

    private final String indent;
    private int indentCount;
    private String output = "";

    public CodeWriter(String indent) {
        this.indent = indent;
    }

    public void writeIndent() {
        for (int i = 0; i < indentCount; i++) {
            output += indent;
        }
    }

    public void write(String str) {
        output += str;
    }

    public void push() {
        indentCount++;
    }

    public void pop() {
        indentCount--;
        if (indentCount < 0) throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return output;
    }
}
