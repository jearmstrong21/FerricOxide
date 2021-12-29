package mackycheese21.ferricoxide.parser;

public class AlternativeSkipException extends RuntimeException {

    public static <T> CommonParser.Parse<T> process(CommonParser.Parse<T> parse) {
        return scanner -> {
            try {
                return CommonParser.attempt(parse).parse(scanner);
            } catch (AlternativeSkipException ignored) {
                return null;
            }
        };
    }

}
