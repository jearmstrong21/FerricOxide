package mackycheese21.ferricoxide.ast.ll.mod;

import mackycheese21.ferricoxide.ast.ll.stmt.LLStatement;
import mackycheese21.ferricoxide.ast.ll.type.LLFunctionType;
import mackycheese21.ferricoxide.ast.ll.type.LLType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LLFunction {

    public final String name; // name <=> export name
    public final boolean inline;
    public final LLFunctionType type; // indexed params => this fully specifies prototype
    public final @Nullable List<LLStatement> statements; // statements == null <=> extern
    public final List<LLType> locals; // locals are indexed => list is sufficient to see all locals

    public LLFunction(String name, boolean inline, LLFunctionType type, @Nullable List<LLStatement> statements, List<LLType> locals) {
        this.name = name;
        this.inline = inline;
        this.type = type;
        this.statements = statements;
        this.locals = locals;
    }

}
