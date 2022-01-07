package mackycheese21.ferricoxide.ast.visitor;

import mackycheese21.ferricoxide.ast.expr.*;
import mackycheese21.ferricoxide.ast.expr.unresolved.*;
import mackycheese21.ferricoxide.ast.type.FOType;
import mackycheese21.ferricoxide.ast.type.StructType;
import mackycheese21.ferricoxide.ast.type.TupleType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StringifyExpressionVisitor implements ExpressionVisitor<String> {
    // TODO ref, explicitRef fields

    @Override
    public String visitUnresolvedIntConstant(UnresolvedIntConstant unresolvedIntConstant) {
        return "" + unresolvedIntConstant.value;
    }

    @Override
    public String visitUnresolvedFloatConstant(UnresolvedFloatConstant unresolvedFloatConstant) {
        return "" + unresolvedFloatConstant.value;
    }

    @Override
    public String visitUnresolvedAccessVar(UnresolvedAccessVar unresolvedAccessVar) {
        return (unresolvedAccessVar.explicitRef ? "&" : "") + unresolvedAccessVar.identifier;
    }

    @Override
    public String visitUnresolvedAccessProperty(UnresolvedAccessProperty unresolvedAccessProperty) {
        if(unresolvedAccessProperty.access.type() == FOType.Access.Type.STRING) {
            return "%s.%s".formatted(unresolvedAccessProperty.aggregate.visit(this), unresolvedAccessProperty.access.string());
        } else {
            return "%s[%s]".formatted(unresolvedAccessProperty.aggregate.visit(this), unresolvedAccessProperty.access.integer());
        }
    }

    @Override
    public String visitAccessVar(AccessVar accessVar) {
        return (accessVar.reference ? "&" : "") + accessVar.name;
    }

    @Override
    public String visitAccessProperty(AccessProperty accessProperty) {
        return "%s.%s".formatted(accessProperty.aggregate.visit(this), accessProperty.index);
    }

    @Override
    public String visitIntConstant(IntConstant intConstant) {
        if (intConstant.type.equals(FOType.I32)) {
            return "" + intConstant.value;
        } else {
            return intConstant.value + " as " + intConstant.type;
        }
    }

    @Override
    public String visitUnary(Unary unary) {
        String s = unary.a.visit(this);
        if (unary.a instanceof Binary) s = "(%s)".formatted(s);
        return "%s%s".formatted(unary.operator.punctuation.str, s);
    }

    @Override
    public String visitBinary(Binary binary) {
        String sa = binary.a.visit(this);
        String sb = binary.b.visit(this);
        if (binary.a instanceof Binary ba && ba.operator.priority < binary.operator.priority)
            sa = "(%s)".formatted(sa);
        if (binary.b instanceof Binary bb && bb.operator.priority < binary.operator.priority)
            sb = "(%s)".formatted(sb);
        return "%s %s %s".formatted(sa, binary.operator.punctType.str, sb);
    }

    @Override
    public String visitIfExpr(IfExpr ifExpr) {
        return "if %s { %s } else { %s }".formatted(ifExpr.condition.visit(this), ifExpr.then.visit(this), ifExpr.otherwise.visit(this));
    }

    @Override
    public String visitBoolConstant(BoolConstant boolConstant) {
        return "" + boolConstant.value;
    }

    @Override
    public String visitCallExpr(CallExpr callExpr) {
        return "%s(%s)".formatted(callExpr.function.visit(this), callExpr.params.stream().map(e -> e.visit(this)).collect(Collectors.joining(", ")));
    }

    @Override
    public String visitUnresolvedStructInit(UnresolvedStructInit unresolvedStructInit) {
        return "%s { %s }".formatted(unresolvedStructInit.struct, unresolvedStructInit.fields.stream()
                .map(p -> "%s: %s".formatted(p.x(), p.y().visit(this)))
                .collect(Collectors.joining(", ")));
    }

    @Override
    public String visitPointerDeref(PointerDeref pointerDeref) {
        return "*%s".formatted(pointerDeref.deref.visit(this));
    }

    @Override
    public String visitCastExpr(CastExpr castExpr) {
        return "%s as %s".formatted(castExpr.value.visit(this), castExpr.target);
    }

    @Override
    public String visitStringConstant(StringConstant stringConstant) {
        return "\"%s\"".formatted(StringConstant.escape(stringConstant.value));
    }

    @Override
    public String visitSizeOf(SizeOf sizeOf) {
        return "sizeof(%s)".formatted(sizeOf.type);
    }

    @Override
    public String visitZeroInit(ZeroInit zeroInit) {
        return "zeroinit(%s)".formatted(zeroInit.type);
    }

    @Override
    public String visitFloatConstant(FloatConstant floatConstant) {
        if (floatConstant.type.equals(FOType.F32)) {
            return "" + floatConstant.value;
        } else {
            return floatConstant.value + " as " + floatConstant.type;
        }
    }

    @Override
    public String visitAggregateInit(AggregateInit aggregateInit) {
        if (aggregateInit.type instanceof TupleType) {
            return "(%s)".formatted(aggregateInit.values.stream().map(e -> e.visit(this) + ",").collect(Collectors.joining(" ")));
        } else if (aggregateInit.type instanceof StructType struct) {
            List<String> fields = new ArrayList<>();
            for (int i = 0; i < struct.fieldNames.size(); i++) {
                fields.add("%s: %s".formatted(struct.fieldNames.get(i), aggregateInit.values.get(i).visit(this)));
            }
            return "%s { %s }".formatted(struct, String.join(", ", fields));
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public String visitArrayIndex(ArrayIndex arrayIndex) {
        return "%s[%s]".formatted(arrayIndex.array.visit(this), arrayIndex.index.visit(this));
    }
}
