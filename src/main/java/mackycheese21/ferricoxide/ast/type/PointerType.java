package mackycheese21.ferricoxide.ast.type;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class PointerType extends FOType {

    public FOType to;

    public PointerType(FOType to) {
        this.to = to;

        this.longName = "ptr[%s]".formatted(to.longName);
        this.explicitName = to.explicitName + "*";
        this.identifier = null;
        this.fields = new LinkedHashMap<>();
        this.methods = new HashMap<>();
        this.pointerType = true;
    }

}
