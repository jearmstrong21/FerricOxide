package mackycheese21.ferricoxide.ast.hl.compile;

import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.hl.stmt.*;
import mackycheese21.ferricoxide.ast.ll.type.LLType;

import java.util.List;

public class HLLocalVisitor implements HLStatementVisitor<Void> {

    private final List<LLType> localTypes;
    private final Identifier modPath;
    private final HLTypeLookup typeLookup;

    public HLLocalVisitor(List<LLType> localTypes, Identifier modPath, HLTypeLookup typeLookup) {
        this.localTypes = localTypes;
        this.modPath = modPath;
        this.typeLookup = typeLookup;
    }

    @Override
    public Void visitAssign(HLAssign stmt) {
        return null;
    }

    @Override
    public Void visitBlock(HLBlock stmt) {
        stmt.statements.forEach(s -> s.visit(this));
        return null;
    }

    @Override
    public Void visitBreak(HLBreak stmt) {
        return null;
    }

    @Override
    public Void visitCallStmt(HLCallStmt stmt) {
        return null;
    }

    @Override
    public Void visitDeclare(HLDeclare stmt) {
        localTypes.add(typeLookup.compile(modPath, stmt.type));
        return null;
    }

    @Override
    public Void visitFor(HLFor stmt) {
        stmt.init.visit(this);
        stmt.update.visit(this);
        stmt.body.forEach(s -> s.visit(this));
        return null;
    }

    @Override
    public Void visitIfStmt(HLIfStmt stmt) {
        stmt.then.forEach(s -> s.visit(this));
        if(stmt.otherwise != null) stmt.otherwise.forEach(s -> s.visit(this));
        return null;
    }

    @Override
    public Void visitLoop(HLLoop stmt) {
        stmt.body.forEach(s -> stmt.visit(this));
        return null;
    }

    @Override
    public Void visitReturn(HLReturn stmt) {
        return null;
    }

    @Override
    public Void visitWhile(HLWhile stmt) {
        stmt.body.forEach(s -> s.visit(this));
        return null;
    }
}
