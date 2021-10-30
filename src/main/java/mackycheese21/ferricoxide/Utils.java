package mackycheese21.ferricoxide;

import java.util.Objects;

public class Utils {

    public static void assertTrue(boolean b) {
        if(!b) throw new RuntimeException();
    }

    public static void assertEquals(Object a, Object b) {
        assertTrue(Objects.equals(a, b));
    }

}
