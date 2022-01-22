package mackycheese21.ferricoxide.ast.hl.mod;

import mackycheese21.ferricoxide.ast.hl.stmt.HLStatement;
import mackycheese21.ferricoxide.parser.token.Span;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FunctionDef extends ModItem {

    public final FunctionPrototype prototype;
    public final boolean inline;
    public final boolean export;
    public final boolean extern;
    public final @Nullable String llvmName;
    public final @Nullable List<HLStatement> body;

    public FunctionDef(Span span, FunctionPrototype prototype, boolean inline, boolean export, boolean extern, @Nullable String llvmName, @Nullable List<HLStatement> body) {
        super(span);
        this.prototype = prototype;
        this.inline = inline;
        this.export = export;
        this.extern = extern;
        this.llvmName = llvmName;
        this.body = body;
    }

    @NotNull
    public String getLlvmName() {
        if(llvmName == null) return prototype.name.toLLVMString();
        return llvmName;
    }

    @Override
    public <T> T visit(ModItemVisitor<T> visitor) {
        return visitor.visitFunctionDef(this);
    }
}
