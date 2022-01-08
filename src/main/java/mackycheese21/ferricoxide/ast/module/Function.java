package mackycheese21.ferricoxide.ast.module;

import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.stmt.Block;
import mackycheese21.ferricoxide.ast.type.FOType;
import mackycheese21.ferricoxide.ast.type.FunctionType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Function {

    public final Identifier name;
    public final boolean inline;
    public FunctionType type;
    public final List<Identifier> paramNames;
    public final Block body;
    public final String llvmName;
    public boolean implicitVoidReturn;
    public @Nullable FOType enclosingType;
    public boolean modRefUpOne; // module references should local reference up one

    public Function(Identifier name, boolean inline, FunctionType type, List<Identifier> paramNames, Block body, String llvmName, boolean implicitVoidReturn, FOType enclosingType, boolean modRefUpOne) {
        this.name = name;
        this.inline = inline;
        this.type = type;
        this.paramNames = paramNames;
        this.body = body;
        this.llvmName = llvmName;
        this.implicitVoidReturn = implicitVoidReturn;
        this.enclosingType = enclosingType;
        this.modRefUpOne = modRefUpOne;
    }

    public Identifier getModPath() {
        if(modRefUpOne) return name.removeLast().removeLast();
        return name.removeLast();
    }

    public boolean isExtern() {
        return body == null;
    }

}
