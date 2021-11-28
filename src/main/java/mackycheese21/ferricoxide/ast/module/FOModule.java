package mackycheese21.ferricoxide.ast.module;

import java.util.Collections;
import java.util.List;

public class FOModule {

    public final List<Function> functions;

    public FOModule(List<Function> functions) {
        this.functions = Collections.unmodifiableList(functions);
    }

}
