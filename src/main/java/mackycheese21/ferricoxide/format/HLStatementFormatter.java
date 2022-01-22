package mackycheese21.ferricoxide.format;

import mackycheese21.ferricoxide.ast.hl.stmt.*;

public class HLStatementFormatter implements HLStatementVisitor<Void> {

    private final CodeWriter writer;
    public final HLExpressionFormatter expr;

    public HLStatementFormatter(CodeWriter writer) {
        this.writer = writer;
        this.expr = new HLExpressionFormatter();
    }


    @Override
    public Void visitAssign(HLAssign stmt) {
        writer.writeIndent();
        writer.write("%s = %s;\n".formatted(stmt.left.visit(expr), stmt.right.visit(expr)));
        return null;
    }

    @Override
    public Void visitBlock(HLBlock stmt) {
        writer.writeIndent();
        writer.write("{\n");

        writer.push();

        stmt.statements.forEach(s -> s.visit(this));

        writer.pop();

        writer.writeIndent();
        writer.write("}\n");
        return null;
    }

    @Override
    public Void visitBreak(HLBreak stmt) {
        writer.writeIndent();
        writer.write("break;\n");
        return null;
    }

    @Override
    public Void visitCallStmt(HLCallStmt stmt) {
        writer.writeIndent();
        writer.write("%s;\n".formatted(stmt.callExpr.visit(expr)));
        return null;
    }

    @Override
    public Void visitDeclare(HLDeclare stmt) {
        writer.writeIndent();
        writer.write("let %s%s = %s;\n".formatted(stmt.name, stmt.type == null ? "" : ": %s".formatted(stmt.type), stmt.value.visit(expr)));
        return null;
    }

    @Override
    public Void visitFor(HLFor stmt) {
//        writer.writeIndent();
//        writer.write("for(");
        throw new UnsupportedOperationException();
//        return null;
    }

    @Override
    public Void visitIfStmt(HLIfStmt stmt) {
        throw new UnsupportedOperationException();
//        return null;
    }

    @Override
    public Void visitLoop(HLLoop stmt) {
        throw new UnsupportedOperationException();
//        return null;
    }

    @Override
    public Void visitReturn(HLReturn stmt) {
        writer.writeIndent();
        writer.write("return%s;\n".formatted(stmt.value == null ? "" : " %s".formatted(stmt.value.visit(expr))));
        return null;
    }

    @Override
    public Void visitWhile(HLWhile stmt) {
        throw new UnsupportedOperationException();
//        return null;
    }
}
