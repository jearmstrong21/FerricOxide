package mackycheese21.ferricoxide.ast.hl.mod;

public interface ModItemVisitor<T> {

    T visitFunctionDef(FunctionDef item);

    T visitGlobalDef(GlobalDef item);

    T visitImplDef(ImplDef item);

    T visitModDef(ModDef item);

    T visitStructDef(StructDef item);

    T visitTraitDef(TraitDef item);

}
