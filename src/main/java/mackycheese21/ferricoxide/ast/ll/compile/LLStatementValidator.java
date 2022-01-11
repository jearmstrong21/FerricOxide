package mackycheese21.ferricoxide.ast.ll.compile;

import mackycheese21.ferricoxide.ast.ll.stmt.*;
import mackycheese21.ferricoxide.ast.ll.type.LLPointerType;
import mackycheese21.ferricoxide.ast.ll.type.LLPrimitiveType;
import mackycheese21.ferricoxide.ast.ll.type.LLType;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LLStatementValidator implements LLStatementVisitor {

    private final LLType returnType;
    private final List<LLType> localTypes;
    private final Map<String, LLType> globalTypes;

    private final LLExpressionValidator expressionValidator;

    public LLStatementValidator(LLType returnType, List<LLType> localTypes, Map<String, LLType> globalTypes, LLExpressionValidator expressionValidator) {
        this.returnType = returnType;
        this.localTypes = localTypes;
        this.globalTypes = globalTypes;
        this.expressionValidator = expressionValidator;
    }

    private boolean inLoop = false;

    private <T> void requireEqual(T actual, T expected) {
        if (!Objects.equals(actual, expected)) throw new UnsupportedOperationException("%s %s".formatted(actual, expected));
    }

//    private LLPointerType requirePointer(LLType type) {
//        if (type instanceof LLPointerType pointer) return pointer;
//        throw new UnsupportedOperationException();
//    }

    private void validateList(List<LLStatement> stmts) {
        for (int i = 0; i < stmts.size(); i++) {
            LLStatement stmt = stmts.get(i);
            stmt.visit(this);
            if ((stmt instanceof LLBreak || stmt instanceof LLReturn) && i != stmts.size() - 1) {
                throw new UnsupportedOperationException();
            }
        }
    }

    @Override
    public void visitAssign(LLAssign stmt) {
        stmt.left.visit(expressionValidator);
        stmt.right.visit(expressionValidator);
//        requireEqual(requirePointer(stmt.left.result).to, stmt.right.result);
        requireEqual(stmt.left.result, new LLPointerType(stmt.right.result));
    }

    @Override
    public void visitBreak(LLBreak stmt) {
        // FIXME: 1/10/22 break labels / number of loops to break upwards
        if (!inLoop) throw new UnsupportedOperationException();
    }

    @Override
    public void visitCallStmt(LLCallStmt stmt) {
        stmt.callExpr.visit(expressionValidator);
    }

    @Override
    public void visitIfStmt(LLIfStmt stmt) {
        stmt.condition.visit(expressionValidator);
        requireEqual(stmt.condition.result, LLPrimitiveType.BOOL);
        validateList(stmt.then);
        validateList(stmt.otherwise);
    }

    @Override
    public void visitLoop(LLLoop stmt) {
        boolean lastInLoop = inLoop;
        inLoop = true;

        validateList(stmt.statements);

        inLoop = lastInLoop;
    }

    @Override
    public void visitReturn(LLReturn stmt) {
        if (stmt.value == null) requireEqual(returnType, LLPrimitiveType.VOID);
        else {
            stmt.value.visit(expressionValidator);
            requireEqual(returnType, stmt.value);
        }
    }
}
