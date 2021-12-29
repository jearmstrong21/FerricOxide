package mackycheese21.ferricoxide.ast.type;

import mackycheese21.ferricoxide.Utils;
import mackycheese21.ferricoxide.ast.Identifier;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class StructType extends FOType {

    public final List<String> fieldNames;

    public StructType(Identifier name, List<String> fieldNames, List<FOType> fieldTypes) {
        Utils.assertTrue(fieldNames.size() == fieldTypes.size());

        this.fieldNames = fieldNames;

        this.longName = "struct " + name.toString();
        this.explicitName = name.toString();
        this.identifier = name;
        this.fields = new LinkedHashMap<>();
        this.methods = new HashMap<>();

        for (int i = 0; i < fieldNames.size(); i++) {
            Access access = Access.string(fieldNames.get(i));
            Utils.assertFalse(fields.containsKey(access));
            fields.put(access, fieldTypes.get(i));
        }
    }

}
