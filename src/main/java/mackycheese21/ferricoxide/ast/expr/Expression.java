package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;
import mackycheese21.ferricoxide.ast.visitor.StringifyVisitor;

public abstract class Expression {

    public final boolean lvalue;

    protected Expression(boolean lvalue) {
        this.lvalue = lvalue;
    }

    public abstract <T> T visit(ExpressionVisitor<T> visitor);

    public Expression makeLValue() {
//        throw AnalysisException.expectedLValue(getClass().getName());
        throw AnalysisException.expectedLValue(visit(new StringifyVisitor("")));
    }

}
