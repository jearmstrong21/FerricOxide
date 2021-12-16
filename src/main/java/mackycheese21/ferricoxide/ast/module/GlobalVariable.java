package mackycheese21.ferricoxide.ast.module;

import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.expr.Expression;
import mackycheese21.ferricoxide.ast.type.ConcreteType;

public class GlobalVariable {

    public ConcreteType type;
    public final Identifier name;
    public final Expression value;

    public GlobalVariable(ConcreteType type, Identifier name, Expression value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

}
