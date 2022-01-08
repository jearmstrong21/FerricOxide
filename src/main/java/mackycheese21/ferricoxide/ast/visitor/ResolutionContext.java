package mackycheese21.ferricoxide.ast.visitor;

import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.type.FOType;

public interface ResolutionContext {

    void setCurrentModPath(Identifier identifier);
    Identifier getCurrentModPath();
    FOType resolveType(FOType type);

}
