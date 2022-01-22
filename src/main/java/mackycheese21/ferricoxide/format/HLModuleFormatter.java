package mackycheese21.ferricoxide.format;

import mackycheese21.ferricoxide.ast.hl.mod.*;

import java.util.List;
import java.util.stream.Collectors;

public class HLModuleFormatter implements ModItemVisitor<Void> {

    private final CodeWriter writer;
    private final HLStatementFormatter stmt;

    private HLModuleFormatter(CodeWriter writer) {
        this.writer = writer;
        stmt = new HLStatementFormatter(writer);
    }

    public static String format(List<ModItem> items) {
        CodeWriter writer = new CodeWriter("\t");
        HLModuleFormatter mod = new HLModuleFormatter(writer);
        items.forEach(i -> i.visit(mod));
        return writer.toString();
    }

    @Override
    public Void visitFunctionDef(FunctionDef item) {
        writer.writeIndent();
        if (item.export)
            writer.write("export%s ".formatted(item.llvmName == null ? "" : "(\"%s\")".formatted(item.llvmName)));
        if (item.extern)
            writer.write("extern%s ".formatted(item.llvmName == null ? "" : "(\"%s\")".formatted(item.llvmName)));
        // TODO &self/self, void return types
        writer.write("fn %s(%s) -> %s".formatted(item.prototype.name.getLast(), item.prototype.params.stream().map(p -> "%s: %s".formatted(p.x(), p.y())).collect(Collectors.joining(", ")), item.prototype.result));
        if (item.extern) writer.write(";\n");
        else {
            writer.write(" {\n");
            writer.push();
            item.body.forEach(s -> s.visit(stmt));
            writer.pop();
            writer.writeIndent();
            writer.write("}\n");
        }
        return null;
    }

    @Override
    public Void visitGlobalDef(GlobalDef item) {
        writer.writeIndent();
        writer.write("let %s: %s = %s;\n".formatted(item.name.getLast(), item.type, item.value.visit(stmt.expr)));
        return null;
    }

    @Override
    public Void visitImplDef(ImplDef item) {
        throw new UnsupportedOperationException();
//        return null;
    }

    @Override
    public Void visitModDef(ModDef item) {
        writer.writeIndent();
        writer.write("mod %s {\n".formatted(item.name));
        writer.push();
        item.items.forEach(i -> i.visit(this));
        writer.pop();
        writer.writeIndent();
        writer.write("}\n");
        return null;
    }

    @Override
    public Void visitStructDef(StructDef item) {
        writer.writeIndent();
        writer.write("struct %s {\n".formatted(item.identifier.getLast()));
        writer.push();
        item.fields.forEach(p -> {
            writer.writeIndent();
            writer.write("%s: %s,\n".formatted(p.x(), p.y()));
        });
        writer.pop();
        writer.writeIndent();
        writer.write("}\n");
        return null;
    }

    @Override
    public Void visitTraitDef(TraitDef item) {
        throw new UnsupportedOperationException();
//        return null;
    }
}
