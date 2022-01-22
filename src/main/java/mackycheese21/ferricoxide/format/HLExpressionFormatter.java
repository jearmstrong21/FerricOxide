package mackycheese21.ferricoxide.format;

import mackycheese21.ferricoxide.ast.hl.expr.*;

import java.util.stream.Collectors;

public class HLExpressionFormatter implements HLExpressionVisitor<String> {
    @Override
    public String visitAccessVar(HLAccess.Var expr) {
        return "%s%s".formatted(expr.ref ? "&" : "", expr.identifier);
    }

    @Override
    public String visitAccessIndex(HLAccess.Index expr) {
        return "%s%s[%s]".formatted(expr.ref ? "&" : "", expr.array.visit(this), expr.index.visit(this));
    }

    @Override
    public String visitAccessPropertyName(HLAccess.Property.Name expr) {
        return "%s%s.%s".formatted(expr.ref ? "&" : "", expr.object.visit(this), expr.name);
    }

    @Override
    public String visitAccessPropertyIndex(HLAccess.Property.Index expr) {
        return "%s%s.%s".formatted(expr.ref ? "&" : "", expr.object.visit(this), expr.index);
    }

    @Override
    public String visitBinary(HLBinary expr) {
        // TODO string priority
        return "(%s) %s (%s)".formatted(expr.left.visit(this), expr.operator.punctType.str, expr.right.visit(this));
    }

    @Override
    public String visitBoolConstant(HLBoolConstant expr) {
        return "%s".formatted(expr.value);
    }

    @Override
    public String visitCallExpr(HLCallExpr expr) {
        return "%s(%s)".formatted(expr.function.visit(this), expr.params.stream().map(e -> e.visit(this)).collect(Collectors.joining(", ")));
    }

    @Override
    public String visitCast(HLCast expr) {
        return "%s as %s".formatted(expr.value.visit(this), expr.target);
    }

    @Override
    public String visitDeref(HLDeref expr) {
        return "*%s".formatted(expr.value.visit(this));
    }

    @Override
    public String visitFloatConstant(HLFloatConstant expr) {
        // TODO format target type on floatconst
        return "%s".formatted(expr.value);
    }

    @Override
    public String visitIfExpr(HLIfExpr expr) {
        return "if %s { %s } else { %s }".formatted(expr.condition.visit(this), expr.then.visit(this), expr.otherwise.visit(this));
    }

    @Override
    public String visitIntConstant(HLIntConstant expr) {
        // TODO format target type on intconst
        return "%s".formatted(expr.value);
    }

    @Override
    public String visitParen(HLParen expr) {
        return "(%s)".formatted(expr.expr.visit(this));
    }

    @Override
    public String visitSizeOf(HLSizeOf expr) {
        return "sizeof(%s)".formatted(expr.target);
    }

    @Override
    public String visitStringConstant(HLStringConstant expr) {
        return "\"%s\"".formatted(HLStringConstant.escape(expr.value));
    }

    @Override
    public String visitStructInit(HLStructInit expr) {
        return "%s { %s }".formatted(expr.struct, expr.fields.stream().map(p -> "%s: %s".formatted(p.x(), p.y().visit(this))).collect(Collectors.joining(", ")));
    }

    @Override
    public String visitTupleInit(HLTupleInit expr) {
        return "(%s)".formatted(expr.values.stream().map(e -> "%s,".formatted(e.visit(this))).collect(Collectors.joining(" ")));
    }

    @Override
    public String visitUnary(HLUnary expr) {
        return "%s(%s)".formatted(expr.operator.punctuation.str, expr.operand.visit(this));
    }

    @Override
    public String visitZeroInit(HLZeroInit expr) {
        return "zeroinit(%s)".formatted(expr.target);
    }
}
