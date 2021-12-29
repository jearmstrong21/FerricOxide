package mackycheese21.ferricoxide.ast.expr.unresolved;

import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.Pair;
import mackycheese21.ferricoxide.ast.expr.Expression;
import mackycheese21.ferricoxide.ast.visitor.ExpressionRequester;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;

public class UnresolvedStructInit extends Expression {

    public final Identifier struct;
    public final List<Pair<String, Expression>> fields;

    public UnresolvedStructInit(Span span, Identifier struct, List<Pair<String, Expression>> fields) {
        super(span);

        this.struct = struct;
        this.fields = fields;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitUnresolvedStructInit(this);
    }

    @Override
    public <T, U> T request(ExpressionRequester<T, U> requester, U request) {
        return requester.visitUnresolvedStructInit(request, this);
    }
}
