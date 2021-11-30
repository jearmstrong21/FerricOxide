package mackycheese21.ferricoxide.ast.visitor;

import mackycheese21.ferricoxide.ast.expr.*;
import mackycheese21.ferricoxide.ast.stmt.*;

import java.util.List;
import java.util.stream.Collectors;

public class StringifyVisitor implements ExpressionVisitor<String>, StatementVisitor<String> {

    private String indent;
    public final String tab;

    public StringifyVisitor(String indent, String tab) {
        this.indent = indent;
        this.tab = tab;
    }

    public StringifyVisitor(String tab) {
        this("", tab);
    }

    public void push() {
        indent += tab;
    }

    public void pop() {
        indent = indent.substring(0, indent.length() - tab.length());
    }

    @Override
    public String visitAccessVar(AccessVar accessVar) {
        return accessVar.name;
    }

    @Override
    public String visitIntConstant(IntConstant intConstant) {
        return "" + intConstant.value;
    }

    @Override
    public String visitBinaryExpr(BinaryExpr binaryExpr) {
        String sa = binaryExpr.a.visit(this);
        String sb = binaryExpr.b.visit(this);
        if (binaryExpr.a instanceof BinaryExpr a && a.operator.priority < binaryExpr.operator.priority)
            sa = "(%s)".formatted(sa);
        if (binaryExpr.b instanceof BinaryExpr b && b.operator.priority < binaryExpr.operator.priority)
            sb = "(%s)".formatted(sb);
        return "%s %s %s".formatted(sa, binaryExpr.operator.punctuation.str, sb);
    }

    @Override
    public String visitUnaryExpr(UnaryExpr unaryExpr) {
        String s = unaryExpr.a.visit(this);
        if (unaryExpr.a instanceof BinaryExpr) s = "(%s)".formatted(s);
        return "%s%s".formatted(unaryExpr.operator.punctuation.str, s);
    }

    @Override
    public String visitIfExpr(IfExpr ifExpr) {
        return String.format("if %s { %s } else { %s }", ifExpr.condition.visit(this), ifExpr.then.visit(this), ifExpr.otherwise.visit(this));
    }

    @Override
    public String visitBoolConstant(BoolConstant boolConstant) {
        return "%s".formatted(boolConstant.value);
    }

    @Override
    public String visitCallExpr(CallExpr callExpr) {
        return "%s(%s)".formatted(callExpr.name, callExpr.params.stream().map(expr -> expr.visit(this)).collect(Collectors.joining(", ")));
    }

    @Override
    public String visitAccessField(AccessField accessField) {
        return "%s.%s".formatted(accessField.object.visit(this), accessField.field);
    }

    @Override
    public String visitStructInit(StructInit structInit) {
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < structInit.fieldNames.size(); i++) {
            if (i > 0) body.append(" ");
            body
                    .append(structInit.fieldNames.get(i))
                    .append(": ")
                    .append(structInit.fieldValues.get(i).visit(this))
                    .append(";");
        }
        return "%s { %s }".formatted(structInit.struct, body.toString());
    }

    @Override
    public String visitPointerDeref(PointerDeref pointerDeref) {
        return "*" + pointerDeref.visit(this);
    }

    @Override
    public String visitCastExpr(CastExpr castExpr) {
        return "(%s) %s".formatted(castExpr.target, castExpr.value.visit(this));
    }

    @Override
    public String visitIndexExpr(IndexExpr indexExpr) {
        return "%s[%s]".formatted(indexExpr.value.visit(this), indexExpr.index.visit(this));
    }

    @Override
    public String visitAssign(Assign assign) {
        return String.format("%s%s = %s;\n", indent, assign.a.visit(this), assign.b.visit(this));
    }

    private String collect(List<Statement> statements) {
        return statements.stream().map(stmt -> stmt.visit(this)).collect(Collectors.joining());
    }

    @Override
    public String visitIfStmt(IfStmt ifStmt) {
        String condition = ifStmt.condition.visit(this);
        push();
        String then = collect(ifStmt.then.statements);
        String otherwise = null;
        if (ifStmt.otherwise != null) {
            otherwise = collect(ifStmt.otherwise.statements);
        }
        pop();
        String result = String.format("%sif %s {\n%s%s}", indent, condition, then, indent);
        if (otherwise == null) {
            result += "\n";
        } else {
            result += String.format(" else {\n%s%s}\n", otherwise, indent);
        }
        return result;
    }

    @Override
    public String visitBlock(Block blockStmt) {
        push();
        String result = collect(blockStmt.statements);
        pop();
        return String.format("%s{\n%s%s}\n", indent, result, indent);
    }

    @Override
    public String visitReturnStmt(ReturnStmt returnStmt) {
        return String.format("%sreturn %s;\n", indent, returnStmt.value.visit(this));
    }

    @Override
    public String visitDeclareVar(DeclareVar declareVar) {
        return String.format("%s%s %s = %s;\n", indent, declareVar.type.toString(), declareVar.name, declareVar.value.visit(this));
    }

    @Override
    public String visitWhileStmt(WhileStmt whileStmt) {
        String condition = whileStmt.condition.visit(this);
        push();
        String body = collect(whileStmt.body.statements);
        pop();
        return String.format("%swhile %s {\n%s%s}\n", indent, condition, body, indent);
    }

    @Override
    public String visitCallStmt(CallStmt callStmt) {
        return "%s%s\n".formatted(indent, visitCallExpr(callStmt.callExpr));
    }
}
