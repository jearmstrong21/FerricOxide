package mackycheese21.ferricoxide.ast.hl.compile;

import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.hl.mod.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Basically a collected List<ModItem>
public class HLModule {

    public final Map<Identifier, StructDef> structDefs;
    public final Map<Identifier, FunctionDef> functionDefs;
    public final Map<Identifier, GlobalDef> globalDefs;
    // TOOD traitDefs

    public HLModule(List<ModItem> items) {
        this(new HashMap<>(), new HashMap<>(), new HashMap<>());
        items.forEach(i -> i.visit(new ModItemVisitor<Void>() {
            @Override
            public Void visitFunctionDef(FunctionDef item) {
                functionDefs.put(item.prototype.name, item);
                return null;
            }

            @Override
            public Void visitGlobalDef(GlobalDef item) {
                globalDefs.put(item.name, item);
                return null;
            }

            @Override
            public Void visitImplDef(ImplDef item) {
                throw new UnsupportedOperationException();
//                return null;
            }

            @Override
            public Void visitModDef(ModDef item) {
                item.items.forEach(i -> i.visit(this));
                return null;
            }

            @Override
            public Void visitStructDef(StructDef item) {
                structDefs.put(item.identifier, item);
                return null;
            }

            @Override
            public Void visitTraitDef(TraitDef item) {
                throw new UnsupportedOperationException();
//                return null;
            }
        }));
    }

    public HLModule(Map<Identifier, StructDef> structDefs, Map<Identifier, FunctionDef> functionDefs, Map<Identifier, GlobalDef> globalDefs) {
        this.structDefs = structDefs;
        this.functionDefs = functionDefs;
        this.globalDefs = globalDefs;
    }
}
