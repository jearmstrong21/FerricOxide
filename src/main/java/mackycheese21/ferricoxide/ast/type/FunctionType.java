package mackycheese21.ferricoxide.ast.type;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class FunctionType extends FOType {

    public FOType result;
    public List<FOType> params;

    public FunctionType(FOType result, List<FOType> params) {
        this.result = result;
        this.params = params;

        this.longName = "fn<%s>(%s)".formatted(params.stream().map(t -> t.longName).collect(Collectors.joining(", ")), result.longName);
        this.explicitName = "fn<%s>(%s)".formatted(params.stream().map(t -> t.explicitName).collect(Collectors.joining(", ")), result.explicitName);
        this.identifier = null;
        this.fields = new LinkedHashMap<>();
        this.methods = new HashMap<>();
    }

}
