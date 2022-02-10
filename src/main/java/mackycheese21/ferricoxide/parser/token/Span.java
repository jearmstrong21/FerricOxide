package mackycheese21.ferricoxide.parser.token;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Objects;

public record Span(Loc start, Loc end, Path file) {

    public static final Span NONE = new Span(null, null, null);

    public static record Loc(int index, int line) implements Comparable<Loc> {
        @Override
        public int compareTo(@NotNull Span.Loc o) {
            int x = Integer.compare(line, o.line);
            if (x != 0) return x;
            return Integer.compare(index, o.index);
        }

        public static Loc min(Loc a, Loc b) {
            if (a.compareTo(b) < 0) {
                return a;
            } else {
                return b;
            }
        }

        @Override
        public String toString() {
            return "%s:%s".formatted(line, index);
        }
    }

    public static Span concat(Span a, Span b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        if(a == NONE || b == NONE) return NONE;
        if (!a.file.equals(b.file)) throw new UnsupportedOperationException();
        Loc ns = Loc.min(a.start, b.start);
        Loc ne = Loc.min(a.end, b.end);
        return new Span(ns, ne, a.file);
    }

    @Override
    public String toString() {
        if(this == NONE) return "no span";
        String line;
        if (start.line == end.line) {
            line = "" + start.line;
        } else {
            line = "%d-%d".formatted(start.line, end.line);
        }
        String index = "%s-%s".formatted(start.index, end.index);
        return "%s:%s".formatted(line, index);
    }

}
