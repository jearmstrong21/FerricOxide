package mackycheese21.ferricoxide.ast.visitor;

import mackycheese21.ferricoxide.ast.module.FOModule;

public interface ModuleVisitor<T> {

    T visit(FOModule module);

}
