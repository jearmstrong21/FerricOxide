package mackycheese21.ferricoxide.nast.ll;

import mackycheese21.ferricoxide.ast.Identifier;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;
import org.bytedeco.llvm.global.LLVM;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LLType {

    public final @NotNull LLVMTypeRef ref;
    public final @NotNull List<LLType> fields;
    public final @Nullable LLType pointerDeref;
    public final @Nullable LLType functionResult;
    public final String str;

    protected LLType(@NotNull LLVMTypeRef ref, @NotNull List<LLType> fields, @Nullable LLType pointerDeref, @Nullable LLType functionResult, String str) {
        this.ref = ref;
        this.fields = fields;
        this.pointerDeref = pointerDeref;
        this.functionResult = functionResult;
        this.str = str;
    }

    public static LLType struct(Identifier name) {
        return new LLType(LLVM.LLVMStructCreateNamed(LLVM.LLVMGetGlobalContext(), name.toLLVMString()), new ArrayList<>(), null, null, name.toLLVMString());
    }

    public static LLType pointer(LLType to) {
        return new LLType(LLVM.LLVMPointerType(to.ref, 0), new ArrayList<>(), to, null, "*" + to);
    }

    public static LLType function(List<LLType> params, LLType result) {
        LLVMTypeRef[] paramRefs = params.stream().map(t -> t.ref).collect(Collectors.toList()).toArray(LLVMTypeRef[]::new);
        return new LLType(LLVM.LLVMFunctionType(result.ref, new PointerPointer<>(params.size()).put(paramRefs), params.size(), 0), new ArrayList<>(), null, result, "fn(%s) -> %s".formatted(params.stream().map(LLType::toString).collect(Collectors.joining(", ")), result));
    }

    private static LLType primitive(String name, LLVMTypeRef ref) {
        return new LLType(ref, new ArrayList<>(), null, null, name);
    }

    public static LLType none() {
        return primitive("none", LLVM.LLVMVoidType());
    }

    public static LLType i32() {
        return primitive("i32", LLVM.LLVMIntType(32));
    }

    public static LLType u32() {
        return primitive("u32", LLVM.LLVMIntType(32));
    }

    public static LLType f32() {
        return primitive("f32", LLVM.LLVMFloatType());
    }

    public void setFields(List<LLType> fields) {
        this.fields.clear();
        this.fields.addAll(fields);
        LLVMTypeRef[] refs = new LLVMTypeRef[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            refs[i] = fields.get(i).ref;
        }
        LLVM.LLVMStructSetBody(ref, new PointerPointer<>(fields.size()).put(refs), fields.size(), 0);
    }

    @Override
    public String toString() {
        return str;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LLType llType = (LLType) o;
        return Objects.equals(str, llType.str);
    }

    @Override
    public int hashCode() {
        return Objects.hash(str);
    }
}
