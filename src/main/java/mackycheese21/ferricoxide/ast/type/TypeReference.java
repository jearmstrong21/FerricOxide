package mackycheese21.ferricoxide.ast.type;

public class TypeReference extends ConcreteType {

    public TypeReference(String name) {
        super(null, false, false, name);
    }

    @Override
    public String toString() {
        return "Reference[%s]".formatted(name);
    }

}
