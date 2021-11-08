package mackycheese21.ferricoxide.token;

public class Span {

    public final int start;
    public final int end;

    public Span(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return String.format("[%s..%s]", start, end);
    }
}
