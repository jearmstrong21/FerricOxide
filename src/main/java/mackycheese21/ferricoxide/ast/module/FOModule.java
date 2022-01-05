package mackycheese21.ferricoxide.ast.module;

import mackycheese21.ferricoxide.ast.type.StructType;

import java.util.List;

// mutable data class
public record FOModule(List<GlobalVariable> globals,
                       List<StructType> structs,
                       List<Function> functions) {

}
