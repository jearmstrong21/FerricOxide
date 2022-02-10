package mackycheese21.ferricoxide;

public record Pair<X, Y>(X x, Y y) {

    @Override
    public String toString() {
        return "(%s, %s)".formatted(x, y);
    }
}
