package mackycheese21.ferricoxide.ast.ll.type;

import mackycheese21.ferricoxide.Pair;
import mackycheese21.ferricoxide.Utils;

import java.util.List;
import java.util.Objects;

public class LLStructType extends LLType {

    public final String name;
    public final List<LLType> fields;

    public LLStructType(String name, List<LLType> fields) {
        this.name = name;
        this.fields = fields;
//        Utils.assertTrue(fields.size() > 0);
    }

    @Override
    public <T> T visit(LLTypeVisitor<T> visitor) {
        return visitor.visitStruct(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LLStructType that = (LLStructType) o;
        return Objects.equals(name, that.name) && Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, fields);
    }
}
