package mackycheese21.ferricoxide.ast.expr;

import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.visitor.ExpressionVisitor;

import java.util.List;

public class StructInit extends Expression {

    public final Identifier struct;
    public final List<String> fieldNames;
    public final List<Expression> fieldValues;

    public StructInit(Identifier struct, List<String> fieldNames, List<Expression> fieldValues) {
        super(false);
        this.struct = struct;
        this.fieldNames = fieldNames;
        this.fieldValues = fieldValues;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visitStructInit(this);
    }
}
