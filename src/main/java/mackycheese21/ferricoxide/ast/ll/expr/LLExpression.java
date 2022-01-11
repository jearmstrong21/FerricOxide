package mackycheese21.ferricoxide.ast.ll.expr;

import mackycheese21.ferricoxide.ast.ll.type.LLType;

public abstract class LLExpression {

    public LLType result;

    public abstract <T> T visit(LLExpressionVisitor<T> visitor);

}
