package mackycheese21.ferricoxide.ast.module;

import mackycheese21.ferricoxide.ast.type.StructType;
import mackycheese21.ferricoxide.ast.visitor.ResolveVisitor;

import java.util.List;

public class FOModule {

    public final List<StructType> structs;
    public final List<Function> functions;

    public FOModule(List<StructType> structs, List<Function> functions) {
        this.structs = structs;
        this.functions = functions;
    }

    public void resolve() {
        new ResolveVisitor().visit(this);
    }

}
