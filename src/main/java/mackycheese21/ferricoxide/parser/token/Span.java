package mackycheese21.ferricoxide.parser.token;

public class Span {

    public final int start;
    public final int end;

    public Span(int start, int end) {
        this.start = start;
        this.end = end;
    }

    private int[] getLineAndPos(int index, String[] lines) {
        int lineNum = 0;
        while (index > lines[lineNum].length() + 1) {
            index -= lines[lineNum].length() + 1;
            lineNum += 1;
        }
        return new int[]{lineNum, index};
    }

    public void highlight(String data) {
        if (start >= end || start < 0 || end >= data.length()) {
            throw new UnsupportedOperationException();
        }
        String[] lines = data.split("\n");
        int[] startLineAndPos = getLineAndPos(start, lines);
        int[] endLineAndPos = getLineAndPos(end, lines);
        if (startLineAndPos[0] != endLineAndPos[0]) throw new UnsupportedOperationException();
        for (int i = 0; i < lines.length; i++) {
            System.out.println("CODE >>> " + lines[i]);
            if (startLineAndPos[0] == i) {
                System.out.println();
                System.out.print("ERR  >>> ");
                for (int x = 0; x < startLineAndPos[1]; x++) {
                    System.out.print(" ");
                }
                for (int x = startLineAndPos[1]; x < endLineAndPos[1]; x++) {
                    System.out.print("^");
                }
                System.out.println();
                System.out.println();
            }
        }
    }

    @Override
    public String toString() {
        return String.format("[%s..%s]", start, end);
    }
}
