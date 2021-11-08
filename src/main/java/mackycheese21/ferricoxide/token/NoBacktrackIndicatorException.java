package mackycheese21.ferricoxide.token;

public class NoBacktrackIndicatorException extends Exception {

    public final ParseException wrapped;


    public NoBacktrackIndicatorException(ParseException wrapped) {
        this.wrapped = wrapped;
    }

}
