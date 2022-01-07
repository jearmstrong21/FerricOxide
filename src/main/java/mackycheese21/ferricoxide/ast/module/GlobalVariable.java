package mackycheese21.ferricoxide.ast.module;

import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.expr.Expression;
import mackycheese21.ferricoxide.ast.type.FOType;

// TODO stack allocation with arrays? see what llvm supports
public class GlobalVariable {

    public FOType type;
    public final Identifier name;
    public Expression value;

    public GlobalVariable(FOType type, Identifier name, Expression value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

}
