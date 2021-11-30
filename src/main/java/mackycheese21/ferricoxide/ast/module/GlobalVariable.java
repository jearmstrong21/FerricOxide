package mackycheese21.ferricoxide.ast.module;

import mackycheese21.ferricoxide.ast.expr.Expression;
import mackycheese21.ferricoxide.ast.type.ConcreteType;

public class GlobalVariable {

    public final ConcreteType type;
    public final String name;
    public final Expression value;

    public GlobalVariable(ConcreteType type, String name, Expression value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

}
