package mackycheese21.ferricoxide.ast.type;

import mackycheese21.ferricoxide.Utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class TupleType extends FOType {

    public List<FOType> types;

    public TupleType(List<FOType> types) {
        Utils.assertFalse(types.size() == 0);
        this.types = types;

        this.longName = "tuple[%s]".formatted(types.stream().map(t -> t.longName).collect(Collectors.joining(", ")));
        this.explicitName = "(%s)".formatted(types.stream().map(t -> t.explicitName).collect(Collectors.joining(", ")));
        this.identifier = null;
        this.fields = new LinkedHashMap<>();
        for (int i = 0; i < types.size(); i++) {
            this.fields.put(Access.integer(i), types.get(i));
        }
        this.methods = new HashMap<>();
    }

}
