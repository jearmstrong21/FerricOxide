package mackycheese21.ferricoxide.format;

import mackycheese21.ferricoxide.ast.hl.expr.HLStringConstant;
import mackycheese21.ferricoxide.ast.ll.expr.*;
import mackycheese21.ferricoxide.ast.ll.mod.LLFunction;
import mackycheese21.ferricoxide.ast.ll.mod.LLModule;
import mackycheese21.ferricoxide.ast.ll.stmt.*;
import mackycheese21.ferricoxide.ast.ll.type.*;

import java.util.stream.Collectors;

public class LLFormatter implements LLTypeVisitor<String>, LLExpressionVisitor<String>, LLStatementVisitor {

    private void visitGlobalItem(String name, LLType type, LLExpression value) {
        writer.write("let %s: %s = %s;\n".formatted(name, type.visit(this), value.visit(this)));
    }

    private void visitStructItem(LLStructType struct) {
        writer.write("struct %s { %s }\n".formatted(struct.name, struct.fields.stream().map(t -> t.visit(this)).collect(Collectors.joining(", "))));
    }

    private void visitFunctionItem(LLFunction function) {
        if (function.inline) writer.write("inline ");
        if (function.statements == null) writer.write("extern ");
        writer.write("fn %s(%s)".formatted(function.name, function.type.params.stream().map(t -> t.visit(this)).collect(Collectors.joining(", "))));
        if (!function.type.result.equals(LLPrimitiveType.VOID)) {
            writer.write(" -> %s".formatted(function.type.result.visit(this)));
        }
        if (function.statements != null) {
            writer.write(" {\n");
            writer.push();
            for (LLStatement stmt : function.statements) {
                stmt.visit(this);
            }
            writer.pop();
            writer.write("}");
        }
        writer.write("\n");
    }

    public static String formatModule(String indent, LLModule module) {
        CodeWriter writer = new CodeWriter(indent);
        LLFormatter llf = new LLFormatter(writer);
        for (String key : module.globalTypes.keySet()) {
            llf.visitGlobalItem(key, module.globalTypes.get(key), module.globalValues.get(key));
        }
        writer.write("\n");
        for (LLStructType value : module.structs.values()) {
            llf.visitStructItem(value);
        }
        writer.write("\n");
        for (LLFunction value : module.functionValues.values()) {
            llf.visitFunctionItem(value);
        }
        return writer.toString();
    }

    @Override
    public String visitFunction(LLFunctionType type) {
        return "fn(%s) -> %s".formatted(type.params.stream().map(t -> t.visit(this)).collect(Collectors.joining(", ")), type.result.visit(this));
    }

    @Override
    public String visitPointer(LLPointerType type) {
        return "%s*".formatted(type.to.visit(this));
    }

    @Override
    public String visitPrimitive(LLPrimitiveType type) {
        return type.name;
    }

    @Override
    public String visitStruct(LLStructType type) {
        return type.name;
    }

    @Override
    public String visitAccessLocal(LLAccess.Local expr) {
        return "%" + expr.index;
    }

    @Override
    public String visitAccessGlobal(LLAccess.Global expr) {
        return expr.name;
    }

    @Override
    public String visitAccessFunction(LLAccess.Function expr) {
        return expr.name;
    }

    @Override
    public String visitAccessIndex(LLAccess.Index expr) {
        return expr.array.visit(this) + "[" + expr.index.visit(this) + "]";
    }

    @Override
    public String visitAccessProperty(LLAccess.Property expr) {
        return expr.object.visit(this) + "." + expr.index;
    }

    @Override
    public String visitBinary(LLBinary expr) {
        String l = expr.left.visit(this);
        String r = expr.right.visit(this);
        if (expr.left instanceof LLBinary binary && binary.operator.priority < expr.operator.priority)
            l = "(%s)".formatted(l);
        if (expr.right instanceof LLBinary binary && binary.operator.priority < expr.operator.priority)
            r = "(%s)".formatted(l);
        return "%s %s %s".formatted(l, expr.operator.punctType.str, r);
    }

    @Override
    public String visitCallExpr(LLCallExpr expr) {
        return "%s(%s)".formatted(expr.function.visit(this), expr.params.stream().map(e -> e.visit(this)).collect(Collectors.joining(", ")));
    }

    @Override
    public String visitCast(LLCast expr) {
        return "%s as %s".formatted(expr.value.visit(this), expr.target);
    }

    @Override
    public String visitDeref(LLDeref expr) {
        return "*%s".formatted(expr.value.visit(this));
    }

    @Override
    public String visitFloatConstant(LLFloatConstant expr) {
        return "%s as %s".formatted(expr.value, expr.target.visit(this));
    }

    @Override
    public String visitIfExpr(LLIfExpr expr) {
        return "if (%s) { %s } else { %s }".formatted(expr.condition.visit(this), expr.then.visit(this), expr.otherwise.visit(this));
    }

    @Override
    public String visitIntConstant(LLIntConstant expr) {
        return "%s as %s".formatted(expr.value, expr.target.visit(this));
    }

    @Override
    public String visitSizeOf(LLSizeOf expr) {
        return "sizeof(%s)".formatted(expr.type.visit(this));
    }

    @Override
    public String visitStringConstant(LLStringConstant expr) {
        return "\"%s\"".formatted(HLStringConstant.escape(expr.value));
    }

    @Override
    public String visitStructInit(LLStructInit expr) {
        return "%s(%s)".formatted(expr.target.visit(this), expr.values.stream().map(e -> e.visit(this)).collect(Collectors.joining(", ")));
    }

    @Override
    public String visitUnary(LLUnary expr) {
        return "%s%s".formatted(expr.operator.punctuation.str, expr.operand.visit(this));
    }

    @Override
    public String visitZeroInit(LLZeroInit expr) {
        return "zeroinit(%s)".formatted(expr.target.visit(this));
    }

    private final CodeWriter writer;

    private LLFormatter(CodeWriter writer) {
        this.writer = writer;
    }

    @Override
    public void visitAssign(LLAssign stmt) {
        writer.writeIndent();
        writer.write("%s = %s;\n".formatted(stmt.left.visit(this), stmt.right.visit(this)));
    }

    @Override
    public void visitBreak(LLBreak stmt) {
        writer.writeIndent();
        writer.write("break;\n");
    }

    @Override
    public void visitCallStmt(LLCallStmt stmt) {
        writer.writeIndent();
        writer.write(stmt.callExpr.visit(this));
        writer.write("\n");
    }

    @Override
    public void visitIfStmt(LLIfStmt stmt) {
        writer.writeIndent();
        writer.write("if (%s) {\n".formatted(stmt.condition.visit(this)));
        writer.push();
        for (LLStatement s : stmt.then) {
            s.visit(this);
        }
        writer.pop();
        writer.writeIndent();
        writer.write("} else {\n");
        writer.push();
        for (LLStatement s : stmt.otherwise) {
            s.visit(this);
        }
        writer.pop();
        writer.writeIndent();
        writer.write("}\n");
    }

    @Override
    public void visitLoop(LLLoop stmt) {
        writer.writeIndent();
        writer.write("loop {\n");
        writer.push();
        for (LLStatement s : stmt.statements) {
            s.visit(this);
        }
        writer.pop();
        writer.writeIndent();
        writer.write("}\n");
    }

    @Override
    public void visitReturn(LLReturn stmt) {
        writer.writeIndent();
        writer.write("return%s;\n".formatted(stmt.value == null ? "" : " " + stmt.value.visit(this)));
    }
}
