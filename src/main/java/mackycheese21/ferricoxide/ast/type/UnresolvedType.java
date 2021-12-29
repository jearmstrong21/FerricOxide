package mackycheese21.ferricoxide.ast.type;

import mackycheese21.ferricoxide.ast.Identifier;

public class UnresolvedType extends FOType {

    public UnresolvedType(Identifier name) {
        this.longName = "ur[%s]".formatted(name.toString());
        this.explicitName = name.toString();
        this.identifier = name;
        this.fields = null;
        this.methods = null;
    }

}
