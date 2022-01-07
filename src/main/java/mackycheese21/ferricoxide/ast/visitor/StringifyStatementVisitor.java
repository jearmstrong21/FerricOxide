package mackycheese21.ferricoxide.ast.visitor;

import mackycheese21.ferricoxide.ast.stmt.*;

public class StringifyStatementVisitor implements StatementVisitor {

    public StringifyStatementVisitor(String indent) {
        this.indent = indent;
    }

    private String output = "";
    public String currentIndent = "";
    private final String indent;

    private void push() {
        currentIndent += indent;
    }

    private void pop() {
        currentIndent = currentIndent.substring(0, currentIndent.length() - indent.length());
    }

    private void write(String str) {
        output += currentIndent + str + "\n";
    }

    public String getOutput() {
        return output;
    }

    private StringifyStatementVisitor copy() {
        return new StringifyStatementVisitor(indent);
    }

    private String postVisit(Statement statement) {
        statement.visit(this);
        return output;
    }

    @Override
    public void visitForStmt(ForStmt forStmt) {
        write("for (%s; %s; %s) {".formatted(copy().postVisit(forStmt.init).trim(), forStmt.condition.stringify(), copy().postVisit(forStmt.update).trim()));
        push();
        forStmt.body.statements.forEach(s -> s.visit(this));
        pop();
        write("}");
    }

    @Override
    public void visitAssign(Assign assign) {
        write("%s = %s;".formatted(assign.a.stringify(), assign.b.stringify()));
    }

    @Override
    public void visitIfStmt(IfStmt ifStmt) {
        write("if (%s) {".formatted(ifStmt.condition.stringify()));
        push();
        ifStmt.then.statements.forEach(s -> s.visit(this));
        pop();
        if (ifStmt.otherwise != null) {
            write("} else {");
            push();
            ifStmt.otherwise.statements.forEach(s -> s.visit(this));
            pop();
        }
        write("}");
    }

    @Override
    public void visitBlock(Block blockStmt) {
        write("{");
        push();
        blockStmt.statements.forEach(s -> s.visit(this));
        pop();
        write("}");
    }

    @Override
    public void visitReturnStmt(ReturnStmt returnStmt) {
        if (returnStmt.value == null) write("return;");
        else write("return %s;".formatted(returnStmt.value.stringify()));
    }

    @Override
    public void visitDeclareVar(DeclareVar declareVar) {
        write("let %s: %s = %s;".formatted(declareVar.name.toString(), declareVar.type.identifier.toString(), declareVar.value.stringify()));
    }

    @Override
    public void visitWhileStmt(WhileStmt whileStmt) {
        write("while (%s) {".formatted(whileStmt.condition.stringify()));
        push();
        whileStmt.body.statements.forEach(s -> s.visit(this));
        pop();
        write("}");
    }

    @Override
    public void visitCallStmt(CallStmt callStmt) {
        write(callStmt.callExpr.stringify());
    }
}
