package mackycheese21.ferricoxide.ast.module;

import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.type.FunctionType;
import mackycheese21.ferricoxide.ast.stmt.Block;

import java.util.List;

public class Function {

    public final Identifier name;
    public final boolean inline;
    public FunctionType type;
    public final List<String> paramNames;
    public final Block body;
    public final String llvmName;

    public Function(Identifier name, boolean inline, FunctionType type, List<String> paramNames, Block body, String llvmName) {
        this.name = name;
        this.inline = inline;
        this.type = type;
        this.paramNames = paramNames;
        this.body = body;
        this.llvmName = llvmName;
    }

    public boolean isExtern() {
        return body == null;
    }

}
