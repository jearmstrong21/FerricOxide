package mackycheese21.ferricoxide.ast.type;

import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.bytedeco.llvm.global.LLVM.*;

public class TypeRegistry {

    private final static Map<FOType, LLVMTypeRef> types = new HashMap<>();

    public static void init() {
        types.put(FOType.VOID, LLVMVoidType());
        types.put(FOType.BOOL, LLVMInt1Type());
        types.put(FOType.I8, LLVMInt8Type());
        types.put(FOType.I16, LLVMInt16Type());
        types.put(FOType.I32, LLVMInt32Type());
        types.put(FOType.I64, LLVMInt64Type());
        types.put(FOType.F32, LLVMFloatType());
        types.put(FOType.F64, LLVMDoubleType());
    }

    @Nullable
    public static LLVMTypeRef attemptLookup(FOType type) {
        if (types.containsKey(type)) return types.get(type);
        if (type instanceof TupleType tuple) {
            LLVMTypeRef typeRef = LLVMStructCreateNamed(LLVMGetGlobalContext(), tuple.explicitName);
            types.put(tuple, typeRef);
            LLVMStructSetBody(typeRef, new PointerPointer<>(tuple.types.size()).put(
                            tuple.types.stream().map(TypeRegistry::forceLookup)
                                    .collect(Collectors.toList()).toArray(LLVMTypeRef[]::new)),
                    tuple.types.size(), 0);
            return typeRef;
        } else if (type instanceof FunctionType function) {
            LLVMTypeRef typeRef = LLVMFunctionType(forceLookup(function.result), new PointerPointer<>(function.params.size()).put(
                            function.params.stream().map(TypeRegistry::forceLookup)
                                    .collect(Collectors.toList()).toArray(LLVMTypeRef[]::new)),
                    function.params.size(), 0);
            types.put(function, typeRef);
            return typeRef;
        } else if (type instanceof PointerType pointer) {
            LLVMTypeRef typeRef = LLVMPointerType(forceLookup(pointer.to), 0);
            types.put(pointer, typeRef);
            return typeRef;
        } else if (type instanceof StructType struct) {
            LLVMTypeRef typeRef = LLVMStructCreateNamed(LLVMGetGlobalContext(), struct.explicitName);
            types.put(struct, typeRef);
            LLVMStructSetBody(typeRef, new PointerPointer<>(struct.fields.size()).put(
                            struct.fields.values().stream().map(TypeRegistry::forceLookup)
                                    .collect(Collectors.toList()).toArray(LLVMTypeRef[]::new)),
                    struct.fields.size(), 0);
            return typeRef;
        }
        throw new UnsupportedOperationException(type.getClass().getSimpleName() + ": " + type.explicitName);
    }

    @NotNull
    public static LLVMTypeRef forceLookup(FOType type) {
        return Objects.requireNonNull(attemptLookup(type));
    }

}
