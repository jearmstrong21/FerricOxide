package mackycheese21.ferricoxide.ast.type;

import mackycheese21.ferricoxide.ast.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class FOType {

    public static class Access {
        public enum Type {
            INTEGER,
            STRING
        }

        private final int integer;
        private final String string;
        public final Type type;

        private Access(int integer, String string, Type type) {
            this.integer = integer;
            this.string = string;
            this.type = type;
        }

        public int integer() {
            if (type != Type.INTEGER) throw new UnsupportedOperationException();
            return integer;
        }

        public String string() {
            if (type != Type.STRING) throw new UnsupportedOperationException();
            return string;
        }

        public static Access integer(int integer) {
            return new Access(integer, null, Type.INTEGER);
        }

        public static Access string(String string) {
            return new Access(0, Objects.requireNonNull(string), Type.STRING);
        }

        @Override
        public String toString() {
            if(type == Type.INTEGER) return "" + integer;
            else return string;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Access access = (Access) o;
            return integer == access.integer && Objects.equals(string, access.string) && type == access.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(integer, string, type);
        }
    }

    public String longName; // fn(x..)->x, x*
    public String explicitName; // explicit ferric oxide name, comparing on this should be enough to compare types, full namespaced here
    public @Nullable Identifier identifier; // nullable for tuples, pointers, functions
    public LinkedHashMap<Access, FOType> fields;
    public Map<String, FunctionType> methods;
    public boolean integerType; // optin
    public int integerWidth; // optin
    public boolean floatType; // optin
    public int floatWidth; // optin

    public int indexOf(Access access) {
        int i = 0;
        for(Access a : fields.keySet()) {
            if(a.equals(access)) return i;
            i++;
        }
        return -1;
    }

    protected FOType() {

    }

    private static FOType _void() {
        FOType type = new PrimitiveType();
        type.longName = "void";
        type.explicitName = "void";
        type.identifier = new Identifier(null, "void");
        type.fields = new LinkedHashMap<>();
        type.methods = new HashMap<>();
        return type;
    }

    private static FOType integer(String name, int width) {
        FOType type = new PrimitiveType();
        type.longName = name;
        type.explicitName = name;
        type.identifier = new Identifier(null, name);
        type.fields = new LinkedHashMap<>();
        type.methods = new HashMap<>();
        type.integerType = true;
        type.integerWidth = width;
        return type;
    }

    private static FOType real(String name, int width) {
        FOType type = new PrimitiveType();
        type.longName = name;
        type.explicitName = name;
        type.identifier = new Identifier(null, name);
        // TODO: null spans for builtin types vs actually using the true spans
        type.fields = new LinkedHashMap<>();
        type.methods = new HashMap<>();
        type.floatType = true;
        type.floatWidth = width;
        return type;
    }

    public static FOType VOID = _void();
    public static FOType BOOL = integer("bool", 1);
    public static FOType I8 = integer("i8", 8);
    public static FOType I16 = integer("i16", 16);
    public static FOType I32 = integer("i32", 32);
    public static FOType I64 = integer("i64", 64);
    public static FOType F32 = real("f32", 32);
    public static FOType F64 = real("f64", 64);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FOType foType = (FOType) o;
        return Objects.equals(explicitName, foType.explicitName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(explicitName);
    }

    @Override
    public String toString() {
        return explicitName;
    }
}
