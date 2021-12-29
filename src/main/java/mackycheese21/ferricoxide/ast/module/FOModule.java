package mackycheese21.ferricoxide.ast.module;

import mackycheese21.ferricoxide.ast.type.StructType;

import java.util.List;

// mutable data class
public class FOModule {

    public final List<GlobalVariable> globals;
    public final List<StructType> structs;
    public final List<Function> functions;

    public FOModule(List<GlobalVariable> globals, List<StructType> structs, List<Function> functions) {
        this.globals = globals;
        this.structs = structs;
        this.functions = functions;
    }

}
