package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.type.FOType;
import mackycheese21.ferricoxide.ast.visitor.ExpressionRequester;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;

public class AggregateInit extends Expression {

    public FOType type;
    public List<Expression> values;

    public AggregateInit(Span span, FOType type, List<Expression> values) {
        super(span);

        this.type = type;
        this.values = values;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitAggregateInit(this);
    }

    @Override
    public <T, U> T request(ExpressionRequester<T, U> requester, U request) {
        return requester.visitAggregateInit(request, this);
    }
}
