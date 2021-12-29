package mackycheese21.ferricoxide.ast.module;

import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.type.FunctionType;
import mackycheese21.ferricoxide.ast.stmt.Block;

import java.util.List;

public class Function {

    public final Identifier name;
    public final boolean inline;
    public FunctionType type;
    public final List<Identifier> paramNames;
    public final Block body;
    public final String llvmName;
    public boolean implicitVoidReturn;

    public Function(Identifier name, boolean inline, FunctionType type, List<Identifier> paramNames, Block body, String llvmName, boolean implicitVoidReturn) {
        this.name = name;
        this.inline = inline;
        this.type = type;
        this.paramNames = paramNames;
        this.body = body;
        this.llvmName = llvmName;
        this.implicitVoidReturn = implicitVoidReturn;
    }

    public boolean isExtern() {
        return body == null;
    }

}
