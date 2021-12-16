package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;
import mackycheese21.ferricoxide.ast.visitor.StringifyVisitor;
import mackycheese21.ferricoxide.ast.visitor.VerboseStringifyVisitor;

public abstract class Expression {

    public final boolean lvalue;

    protected Expression(boolean lvalue) {
        this.lvalue = lvalue;
    }

    public abstract <T> T visit(ExpressionVisitor<T> visitor);

    public Expression makeLValue() {
//        throw AnalysisException.expectedLValue(getClass().getName());
//        throw AnalysisException.expectedLValue(visit(new StringifyVisitor("")));
        return this;
    }

    public final String stringify() {
        return visit(new StringifyVisitor(""));
    }

    public final String verbose() {
        return visit(new VerboseStringifyVisitor());
    }

}
