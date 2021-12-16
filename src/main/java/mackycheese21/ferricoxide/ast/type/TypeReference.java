package mackycheese21.ferricoxide.ast.type;

import mackycheese21.ferricoxide.ast.Identifier;

public class TypeReference extends ConcreteType {

    public final Identifier usePath;
    public final Identifier identifier;

    public TypeReference(Identifier usePath, Identifier identifier) {
        super(null, false, false, identifier.toString());
        this.usePath = usePath;
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        return "Reference[%s]".formatted(name);
    }

}
