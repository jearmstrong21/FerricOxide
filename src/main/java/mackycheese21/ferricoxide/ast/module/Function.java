package mackycheese21.ferricoxide.ast.module;

import mackycheese21.ferricoxide.ast.type.FunctionType;
import mackycheese21.ferricoxide.ast.stmt.Block;

import java.util.List;

public class Function {

    public final String name;
    public final boolean inline;
    public FunctionType type;
    public final List<String> paramNames;
    public final Block body;

    public Function(String name, boolean inline, FunctionType type, List<String> paramNames, Block body) {
        this.name = name;
        this.inline = inline;
        this.type = type;
        this.paramNames = paramNames;
        this.body = body;
    }

    public boolean isExtern() {
        return body == null;
    }

}
