package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.Pair;
import mackycheese21.ferricoxide.ast.type.FOType;
import mackycheese21.ferricoxide.ast.visitor.ExpressionRequester;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;
import mackycheese21.ferricoxide.ast.visitor.StringifyExpressionVisitor;
import mackycheese21.ferricoxide.parser.token.Span;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Expression {

    public final Span span;
    public FOType result;

    protected Expression(Span span) {
        this.span = span;
    }

    public Expression result(FOType result) {
        this.result = result;
        return this;
    }

    public abstract <T> T visit(ExpressionVisitor<T> visitor);

    public abstract <T, U> T request(ExpressionRequester<T, U> requester, U request);

    public final String stringify() {
        return visit(new StringifyExpressionVisitor());
    }

    /**
     * verbose stringify
     */
    public final String toString() {
        List<Pair<String, String>> fields = new ArrayList<>();
        for (Field field : getClass().getDeclaredFields()) {
            if (!field.getName().equals("result") && !field.getName().equals("span")) {
                try {
                    fields.add(new Pair<>(field.getName(), field.get(this).toString()));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return "%s{%s}".formatted(
                getClass().getSimpleName(),
                fields.stream().map(pair -> "%s=%s".formatted(pair.x(), pair.y())).collect(Collectors.joining(", "))
        );
    }


}
