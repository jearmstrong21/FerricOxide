package mackycheese21.ferricoxide.ast.hl.stmt;

import mackycheese21.ferricoxide.ast.hl.expr.HLExpression;
import mackycheese21.ferricoxide.parser.token.Span;

public class HLAssign extends HLStatement {

    public final HLExpression left;
    public final HLExpression right;

    // TODO: 1/8/22 assign operators like +=

    public HLAssign(Span span, HLExpression left, HLExpression right) {
        super(span);
        this.left = left;
        this.right = right;
    }

    @Override
    public <T> T visit(HLStatementVisitor<T> visitor) {
        return visitor.visitAssign(this);
    }
}
