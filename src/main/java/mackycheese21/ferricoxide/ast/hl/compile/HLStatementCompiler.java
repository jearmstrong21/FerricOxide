package mackycheese21.ferricoxide.ast.hl.compile;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.MapStack;
import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.hl.mod.FunctionDef;
import mackycheese21.ferricoxide.ast.hl.stmt.*;
import mackycheese21.ferricoxide.ast.hl.type.HLKeywordType;
import mackycheese21.ferricoxide.ast.hl.type.HLType;
import mackycheese21.ferricoxide.ast.ll.expr.LLAccess;
import mackycheese21.ferricoxide.ast.ll.expr.LLCallExpr;
import mackycheese21.ferricoxide.ast.ll.expr.LLExpression;
import mackycheese21.ferricoxide.ast.ll.stmt.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HLStatementCompiler implements HLStatementVisitor<List<LLStatement>> {

    private final List<HLType> localTypes;
    private final MapStack<String, Integer> localIndices;

    private final HLTypeLookup typeCollector;

    private final HLType returnType;
    private final HLExpressionCompiler expressionCompiler;

    public HLStatementCompiler(
            Identifier modPath,
            HLTypeLookup typeLookup,
            FunctionDef functionDef) {
        this.localTypes = new ArrayList<>();
        this.localIndices = new MapStack<>();
        this.typeCollector = typeLookup;
        this.returnType = functionDef.prototype.result;
        this.expressionCompiler = new HLExpressionCompiler(localTypes, localIndices, modPath, typeLookup);
        localIndices.push();
        for (int i = 0; i < functionDef.prototype.params.size(); i++) {
            localTypes.add(functionDef.prototype.params.get(i).y());
            localIndices.put(functionDef.prototype.params.get(i).x(), i);
        }
    }

    private boolean inLoop = false;

    private List<LLStatement> compileList(@Nullable List<HLStatement> stmts) {
        if (stmts == null) return new ArrayList<>();
        return stmts.stream().collect(Collectors.flatMapping(s -> s.visit(this).stream(), Collectors.toList()));
    }

    @Override
    public List<LLStatement> visitAssign(HLAssign stmt) {
        LLExpression left = stmt.left.visit(expressionCompiler);
        LLExpression right = stmt.right.visit(expressionCompiler);
        return List.of(new LLAssign(left, right));
    }

    @Override
    public List<LLStatement> visitBlock(HLBlock stmt) {
        return compileList(stmt.statements);
    }

    @Override
    public List<LLStatement> visitBreak(HLBreak stmt) {
        if (inLoop) return List.of(new LLBreak());
        throw new UnsupportedOperationException();
    }

    @Override
    public List<LLStatement> visitCallStmt(HLCallStmt stmt) {
        return List.of(new LLCallStmt((LLCallExpr) stmt.callExpr.visit(expressionCompiler)));
    }

    @Override
    public List<LLStatement> visitDeclare(HLDeclare stmt) {
        int n = localTypes.size();
        localTypes.add(stmt.type);//TODO resolve type
        if (localIndices.containsKey(stmt.name))
            throw new AnalysisException(stmt.span, "already declared variable name");
        localIndices.put(stmt.name, n);
        return List.of(new LLAssign(new LLAccess.Local(n), stmt.value.visit(expressionCompiler)));
    }

    @Override
    public List<LLStatement> visitFor(HLFor stmt) {
        List<LLStatement> init = stmt.init.visit(this);
        LLExpression condition = stmt.condition.visit(expressionCompiler);
        List<LLStatement> update = stmt.update.visit(this);
        List<LLStatement> body = compileList(stmt.body);

        List<LLStatement> newBody = new ArrayList<>();
        newBody.addAll(body);
        newBody.addAll(update);
        newBody.add(new LLIfStmt(condition, new ArrayList<>(), List.of(new LLBreak())));

        List<LLStatement> result = new ArrayList<>();
        result.addAll(init);
        result.add(new LLLoop(newBody));

        return result;
    }

    @Override
    public List<LLStatement> visitIfStmt(HLIfStmt stmt) {
        LLExpression condition = stmt.condition.visit(expressionCompiler);
        List<LLStatement> then = compileList(stmt.then);
        List<LLStatement> otherwise = compileList(stmt.otherwise);
        return List.of(new LLIfStmt(condition, then, otherwise));
    }

    @Override
    public List<LLStatement> visitLoop(HLLoop stmt) {
        return List.of(new LLLoop(compileList(stmt.body)));
    }

    @Override
    public List<LLStatement> visitReturn(HLReturn stmt) {
        if (stmt.value == null) {
            if (!(returnType instanceof HLKeywordType keyword && keyword.type == HLKeywordType.Type.VOID)) {
                throw new AnalysisException(stmt.span, "given void return, expected %s".formatted(returnType));
            }
            return List.of(new LLReturn(null));
        } else {
            LLExpression value = stmt.value.visit(expressionCompiler);
            stmt.value.requireResult(returnType);
            return List.of(new LLReturn(value));
        }
    }

    @Override
    public List<LLStatement> visitWhile(HLWhile stmt) {
        LLExpression condition = stmt.condition.visit(expressionCompiler);
        List<LLStatement> body = compileList(stmt.body);

        List<LLStatement> newBody = new ArrayList<>();
        newBody.addAll(body);
        newBody.add(new LLIfStmt(condition, new ArrayList<>(), List.of(new LLBreak())));

        return List.of(new LLLoop(newBody));
    }
}
