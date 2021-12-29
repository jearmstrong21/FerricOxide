package mackycheese21.ferricoxide.compile;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.MapStack;
import mackycheese21.ferricoxide.Utils;
import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.expr.*;
import mackycheese21.ferricoxide.ast.expr.unresolved.*;
import mackycheese21.ferricoxide.ast.type.*;
import mackycheese21.ferricoxide.ast.visitor.ExpressionRequester;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

// THIS CLASS MUST SET result FIELD ON RETURNED EXPRESSIONS
public class ExpressionValidator implements ExpressionRequester<Expression, FOType> {

    private final MapStack<Identifier, FOType> localVariables;
    private final Map<Identifier, FunctionType> functions;
    private final Map<Identifier, FOType> globalVariables;
    private final Identifier namespacePath;
    private final Map<Identifier, StructType> structs;
    private final Function<FOType, FOType> typeResolver;

    public ExpressionValidator(
            MapStack<Identifier, FOType> localVariables,
            Map<Identifier, FunctionType> functions,
            Map<Identifier, FOType> globalVariables,
            Identifier namespacePath,
            Map<Identifier, StructType> structs,
            Function<FOType, FOType> typeResolver
    ) {
        this.localVariables = localVariables;
        this.functions = functions;
        this.globalVariables = globalVariables;
        this.namespacePath = namespacePath;
        this.structs = structs;
        this.typeResolver = typeResolver;
    }

    @Override
    public Expression visitUnresolvedIntConstant(FOType request, UnresolvedIntConstant unresolvedIntConstant) {
        // TODO: check for overflow constants
        if (request == null)
            return new IntConstant(unresolvedIntConstant.span, FOType.I32, unresolvedIntConstant.value).result(FOType.I32);

        if (request.integerType)
            return new IntConstant(unresolvedIntConstant.span, request, unresolvedIntConstant.value).result(request);
        if (request.floatType)
            return new FloatConstant(unresolvedIntConstant.span, request, unresolvedIntConstant.value).result(request);

        return new IntConstant(unresolvedIntConstant.span, FOType.I32, unresolvedIntConstant.value).result(FOType.I32);
    }

    @Override
    public Expression visitUnresolvedFloatConstant(FOType request, UnresolvedFloatConstant unresolvedFloatConstant) {
        if (request == null)
            return new FloatConstant(unresolvedFloatConstant.span, FOType.F32, unresolvedFloatConstant.value).result(FOType.F32);
        if (request.floatType)
            return new FloatConstant(unresolvedFloatConstant.span, request, unresolvedFloatConstant.value).result(request);

        return new FloatConstant(unresolvedFloatConstant.span, FOType.F32, unresolvedFloatConstant.value).result(FOType.F32);
    }

    /**
     * This is where the identifier resolution happens for variables. For types see TypeValidatorVisitor#resolve
     */
    @Override
    public Expression visitUnresolvedAccessVar(FOType request, UnresolvedAccessVar unresolvedAccessVar) {
        Identifier localIdentifier = unresolvedAccessVar.identifier;

        Identifier globalNamespaceIdentifier = unresolvedAccessVar.identifier;

        // BEWARE OF fucky spans
        Identifier localNamespaceIdentifier = Identifier.concat(namespacePath, unresolvedAccessVar.identifier);

        if (localVariables.containsKey(localIdentifier)) {
            FOType resultType = localVariables.get(localIdentifier);
            System.out.println("result " + resultType.longName);
            boolean reference = false;
            if (unresolvedAccessVar.explicitRef || (request != null && request.equals(new PointerType(resultType)))) {
                reference = true;
                resultType = new PointerType(resultType);
            }

            return new AccessVar(unresolvedAccessVar.span, reference, AccessVar.Type.LOCAL, localIdentifier)
                    .result(resultType);
        }
        if (functions.containsKey(localNamespaceIdentifier)) {
            if (unresolvedAccessVar.explicitRef)
                throw new AnalysisException(unresolvedAccessVar.span, "cannot reference function");
            return new AccessVar(unresolvedAccessVar.span, false, AccessVar.Type.FUNCTION, localNamespaceIdentifier)
                    .result(functions.get(localNamespaceIdentifier));
        }
        if (functions.containsKey(globalNamespaceIdentifier)) {
            if (unresolvedAccessVar.explicitRef)
                throw new AnalysisException(unresolvedAccessVar.span, "cannot reference function");
            return new AccessVar(unresolvedAccessVar.span, false, AccessVar.Type.FUNCTION, globalNamespaceIdentifier)
                    .result(functions.get(globalNamespaceIdentifier));
        }
        if (globalVariables.containsKey(localNamespaceIdentifier)) {
            FOType resultType = globalVariables.get(localNamespaceIdentifier);
            boolean reference = false;
            if (unresolvedAccessVar.explicitRef || Objects.equals(request, new PointerType(resultType))) {
                reference = true;
                resultType = new PointerType(resultType);
            }

            return new AccessVar(unresolvedAccessVar.span, reference, AccessVar.Type.GLOBAL, localNamespaceIdentifier)
                    .result(resultType);
        }
        if (globalVariables.containsKey(globalNamespaceIdentifier)) {
            FOType resultType = globalVariables.get(globalNamespaceIdentifier);
            boolean reference = false;
            if (unresolvedAccessVar.explicitRef || (request != null && request.equals(new PointerType(request)))) {
                reference = true;
                resultType = new PointerType(resultType);
            }

            return new AccessVar(unresolvedAccessVar.span, reference, AccessVar.Type.GLOBAL, globalNamespaceIdentifier)
                    .result(resultType);
        }
        throw new AnalysisException(unresolvedAccessVar.span, "cannot resolve identifier %s (localNamespace=%s, globalNamespace=%s)".formatted(unresolvedAccessVar.identifier, localNamespaceIdentifier, globalNamespaceIdentifier));
    }

    private int accessToIndex(Span span, FOType aggregate, FOType.Access access) {
        int index = aggregate.indexOf(access);
        if (index == -1) throw new AnalysisException(span, "no such access %s on type %s".formatted(access, aggregate));
        return index;
    }

    @Override
    public Expression visitUnresolvedAccessProperty(FOType request, UnresolvedAccessProperty unresolvedAccessProperty) {
        unresolvedAccessProperty.aggregate = unresolvedAccessProperty.aggregate.request(this, null);
        System.out.println("before: " + unresolvedAccessProperty.aggregate);
        if(!(unresolvedAccessProperty.aggregate.result instanceof PointerType))
            unresolvedAccessProperty.aggregate = unresolvedAccessProperty.aggregate.request(this, new PointerType(unresolvedAccessProperty.aggregate.result));
        System.out.println("after: " + unresolvedAccessProperty.aggregate);
        FOType aggregate = Utils.expectPointer(unresolvedAccessProperty.aggregate.span, unresolvedAccessProperty.aggregate.result).to;
        if(unresolvedAccessProperty.arrowAccess) throw new UnsupportedOperationException("EV L141 no arrow access");
//        aggregate = Utils.expectPointer(unresolvedAccessProperty.aggregate.span, aggregate).to;
        FOType resultType = aggregate.fields.get(unresolvedAccessProperty.access);
        boolean ref = false;
        if(unresolvedAccessProperty.explicitRef || (request != null && request.equals(new PointerType(resultType)))) {
            resultType = new PointerType(resultType);
            ref = true;
        }
        return new AccessProperty(unresolvedAccessProperty.span,
                unresolvedAccessProperty.aggregate,
                accessToIndex(unresolvedAccessProperty.span, aggregate, unresolvedAccessProperty.access),
                unresolvedAccessProperty.arrowAccess,
                ref
        ).result(resultType);
    }

    @Override
    public Expression visitAccessVar(FOType request, AccessVar accessVar) {
        Utils.assertTrue(accessVar.result != null);
        if(!accessVar.reference && request != null && request.equals(new PointerType(accessVar.result))) {
            accessVar.reference = true;
            accessVar.result = new PointerType(accessVar.result);
        }
        return accessVar;
    }

    @Override
    public Expression visitAccessProperty(FOType request, AccessProperty accessProperty) {
        Utils.assertTrue(accessProperty.result != null);
        if(!accessProperty.ref && request != null && request.equals(new PointerType(accessProperty.result))) {
            accessProperty.ref = true;
            accessProperty.result = new PointerType(accessProperty.result);
        }
        return accessProperty;
    }

    @Override
    public Expression visitIntConstant(FOType request, IntConstant intConstant) {
        return intConstant;
    }

    @Override
    public Expression visitUnary(FOType request, Unary unary) {
        unary.a = unary.a.request(this, null);
        return unary.result(unary.operator.validate(unary.span, unary.a.result));
    }

    @Override
    public Expression visitBinary(FOType request, Binary binary) {
        binary.a = binary.a.request(this, null);
        binary.b = binary.b.request(this, binary.a.result);
        Utils.requireType(binary.a.result, binary.b);
        return binary.result(binary.operator.validate(binary.span, binary.a.result));
    }

    @Override
    public Expression visitIfExpr(FOType request, IfExpr ifExpr) {
        ifExpr.condition = ifExpr.condition.request(this, FOType.BOOL);
        ifExpr.then = ifExpr.then.request(this, null);
        ifExpr.otherwise = ifExpr.otherwise.request(this, ifExpr.then.result);
        Utils.requireType(ifExpr.then.result, ifExpr.otherwise);
        return ifExpr.result(ifExpr.then.result);
    }

    @Override
    public Expression visitBoolConstant(FOType request, BoolConstant boolConstant) {
        return boolConstant.result(FOType.BOOL);
    }

    @Override
    public Expression visitCallExpr(FOType request, CallExpr callExpr) {
        callExpr.function = callExpr.function.request(this, null);
        callExpr.params = callExpr.params.stream().map(e -> e.request(this, null)).collect(Collectors.toList());
        FunctionType functionType = Utils.expectFunction(callExpr.function.span, callExpr.function.result);
        if (callExpr.params.size() != functionType.params.size())
            throw new AnalysisException(callExpr.span, "wrong # of args");
        // TODO varargs
        for (int i = 0; i < callExpr.params.size(); i++) {
            Utils.requireType(functionType.params.get(i), callExpr.params.get(i));
        }
        return callExpr.result(functionType.result);
    }

    @Override
    public Expression visitUnresolvedStructInit(FOType request, UnresolvedStructInit unresolvedStructInit) {
        Identifier localIdentifier = Identifier.concat(namespacePath, unresolvedStructInit.struct);
        Identifier globalIdentifier = unresolvedStructInit.struct;
        StructType structType;
        if (structs.containsKey(localIdentifier)) {
            structType = structs.get(localIdentifier);
        } else if (structs.containsKey(globalIdentifier)) {
            structType = structs.get(globalIdentifier);
        } else {
            throw new AnalysisException(unresolvedStructInit.struct.span, "no struct found");
        }
        if (structType.fields.size() != unresolvedStructInit.fields.size()) {
            throw new AnalysisException(unresolvedStructInit.span, "wrong number of fields");
        }
        List<Expression> values = new ArrayList<>();
        for (FOType.Access access : structType.fields.keySet()) { // ORDERED TRAVERSAL
            // index = index in unresolvedStructInit of the current access
            int index = -1;
            for (int i = 0; i < unresolvedStructInit.fields.size(); i++) {
                if (access.string().equals(unresolvedStructInit.fields.get(i).x())) {
                    if (index != -1)
                        throw new AnalysisException(unresolvedStructInit.fields.get(i).y().span, "duplicate field in init");
                    index = i;
                }
            }
            if (index == -1)
                throw new AnalysisException(unresolvedStructInit.span, "struct init missing field %s".formatted(access.string()));
            Expression expr = unresolvedStructInit.fields.get(index).y().request(this, structType.fields.get(access));
            Utils.requireType(structType.fields.get(access), expr);
            values.add(expr);
        }
        return new AggregateInit(unresolvedStructInit.span, structType, values).result(structType);
    }

    @Override
    public Expression visitPointerDeref(FOType request, PointerDeref pointerDeref) {
        pointerDeref.deref = pointerDeref.deref.request(this, null);
        PointerType pointer = Utils.expectPointer(pointerDeref.deref.span, pointerDeref.deref.result);
        return pointerDeref.result(pointer.to);
    }

    @Override
    public Expression visitCastExpr(FOType request, CastExpr castExpr) {
        castExpr.target = typeResolver.apply(castExpr.target);
        castExpr.value = castExpr.value.request(this, castExpr.target);
        System.out.println(castExpr);
        if (CastOperator.validate(castExpr.value.result, castExpr.target)) {
            return castExpr.result(castExpr.target);
        } else {
            throw new AnalysisException(castExpr.span, "cannot cast %s to %s".formatted(castExpr.value.result, castExpr.target));
        }
    }

    @Override
    public Expression visitStringConstant(FOType request, StringConstant stringConstant) {
        return stringConstant.result(new PointerType(FOType.I8)); // i8*
    }

    @Override
    public Expression visitSizeOf(FOType request, SizeOf sizeOf) {
        sizeOf.type = typeResolver.apply(sizeOf.type);
        return sizeOf.result(FOType.I32);
    }

    @Override
    public Expression visitZeroInit(FOType request, ZeroInit zeroInit) {
        zeroInit.type = typeResolver.apply(zeroInit.type);
        return zeroInit.result(zeroInit.type);
    }

    @Override
    public Expression visitFloatConstant(FOType request, FloatConstant floatConstant) {
        return floatConstant;
    }

    @Override
    public Expression visitAggregateInit(FOType request, AggregateInit aggregateInit) {
        return aggregateInit;
    }

    @Override
    public Expression visitArrayIndex(FOType request, ArrayIndex arrayIndex) {
        arrayIndex.array = arrayIndex.array.request(this, null);
        arrayIndex.index = arrayIndex.index.request(this, FOType.I32);
        FOType elementType = Utils.expectPointer(arrayIndex.array.span, arrayIndex.array.result).to;
        Utils.requireType(FOType.I32, arrayIndex.index);
        if(arrayIndex.ref || (request != null && request.equals(new PointerType(elementType)))) {
            arrayIndex.ref = true;
            elementType = new PointerType(elementType);
        }
        return arrayIndex.result(elementType);
    }
}
