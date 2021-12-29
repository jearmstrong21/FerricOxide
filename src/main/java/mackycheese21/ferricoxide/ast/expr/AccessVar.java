package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.visitor.ExpressionRequester;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;
import mackycheese21.ferricoxide.parser.token.Span;

public class AccessVar extends Expression {

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitAccessVar(this);
    }

    public enum Type {
        /** variable, global namespace */
        GLOBAL,

        /** function, global namespace */
        FUNCTION,

        /** local, no namespace / parameter-level */
        LOCAL
    }

    public boolean reference;
    public Type type;
    public Identifier name;

    public AccessVar(Span span, boolean reference, Type type, Identifier name) {
        super(span);

        this.reference = reference;
        this.type = type;
        this.name = name;
    }

    @Override
    public <T, U> T request(ExpressionRequester<T, U> requester, U request) {
        return requester.visitAccessVar(request, this);
    }
}
