package mackycheese21.ferricoxide.ast.module;

import mackycheese21.ferricoxide.ast.type.StructType;

import java.util.List;
import java.util.Objects;

// mutable data class
public final class FOModule {
    public final List<GlobalVariable> globals;
    public final List<StructType> structs;
    public final List<Function> functions;
    public String formatted;

    public FOModule(List<GlobalVariable> globals,
                    List<StructType> structs,
                    List<Function> functions,
                    String formatted) {
        this.globals = globals;
        this.structs = structs;
        this.functions = functions;
        this.formatted = formatted;
    }


}
