package mackycheese21.ferricoxide.ast.hl.expr;

import mackycheese21.ferricoxide.Pair;
import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;

public class HLStructInit extends HLExpression {

    public final Identifier struct;
    public final List<Pair<String, HLExpression>> fields;

    public HLStructInit(Span span, Identifier struct, List<Pair<String, HLExpression>> fields) {
        super(span);
        this.struct = struct;
        this.fields = fields;
    }

    @Override
    public <T> T visit(HLExpressionVisitor<T> visitor) {
        return visitor.visitStructInit(this);
    }
}
