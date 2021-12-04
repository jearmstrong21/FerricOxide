package mackycheese21.ferricoxide.ast.type;

import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.bytedeco.llvm.global.LLVM.LLVMFunctionType;

public class FunctionType extends ConcreteType {
    public final ConcreteType result;
    public final List<ConcreteType> params;

    private final static List<FunctionType> registry = new ArrayList<>();

    private FunctionType(ConcreteType result, List<ConcreteType> params) {
        super(params.stream().allMatch(t -> t.complete) && result.complete ? LLVMFunctionType(
                result.typeRef,
                new PointerPointer<>(params.stream().map(t -> t.typeRef)
                        .collect(Collectors.toList()).toArray(LLVMTypeRef[]::new)),
                params.size(), 0 /* false */) : null, false, true, "%s(%s)".formatted(result.name, params.stream().map(p -> p.name)
                .collect(Collectors.joining(", "))));
        this.result = result;
        this.params = params;
    }

    @Override
    public String toString() {
        return "Function[%s]".formatted("%s(%s)".formatted(result, params.stream().map(ConcreteType::toString)
                .collect(Collectors.joining(", "))));
    }

    public static FunctionType of(ConcreteType result, List<ConcreteType> params) {
        if (!result.complete || params.stream().anyMatch(t -> !t.complete)) {
            return new FunctionType(result, params);
        }
        for (FunctionType functionType : registry) {
            if (functionType.result == result && functionType.params.equals(params)) {
                return functionType;
            }
        }
        registry.add(new FunctionType(result, params));
        return registry.get(registry.size() - 1);
    }
}
