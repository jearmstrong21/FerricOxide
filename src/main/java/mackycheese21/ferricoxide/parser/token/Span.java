package mackycheese21.ferricoxide.parser.token;

import java.nio.file.Path;

public class Span {

    public final int start, line, distance;
    public final Path file;

    public Span(int start, int line, int distance, Path file) {
        this.start = start;
        this.line = line;
        this.distance = distance;
        this.file = file;
    }
}
