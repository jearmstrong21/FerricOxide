package mackycheese21.ferricoxide.nast.ll;

import mackycheese21.ferricoxide.Identifier;
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
    public final boolean signedInteger, unsignedInteger, floatType;

    protected LLType(@NotNull LLVMTypeRef ref, @NotNull List<LLType> fields, @Nullable LLType pointerDeref, @Nullable LLType functionResult, String str, boolean signedInteger, boolean unsignedInteger, boolean floatType) {
        this.ref = ref;
        this.fields = fields;
        this.pointerDeref = pointerDeref;
        this.functionResult = functionResult;
        this.str = str;
        this.signedInteger = signedInteger;
        this.unsignedInteger = unsignedInteger;
        this.floatType = floatType;
    }

    public static LLType emptyStruct(Identifier name) {
        return new LLType(LLVM.LLVMStructCreateNamed(LLVM.LLVMGetGlobalContext(), name.toLLVMString()), new ArrayList<>(), null, null, name.toLLVMString(), false, false, false);
    }

    public static LLType tuple(List<LLType> fields) {
        LLVMTypeRef[] refs = new LLVMTypeRef[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            refs[i] = fields.get(i).ref;
        }
        return new LLType(
                LLVM.LLVMStructType(new PointerPointer<>(fields.size()).put(refs), fields.size(), 0),
                fields, null, null,
                "(%s)".formatted(fields.stream().map(LLType::toString).collect(Collectors.joining(", "))),
                false, false, false);
    }

    public static LLType pointer(LLType to) {
        return new LLType(LLVM.LLVMPointerType(to.ref, 0), new ArrayList<>(), to, null, "*" + to, false, false, false);
    }

    public static LLType function(List<LLType> params, LLType result) {
        LLVMTypeRef[] paramRefs = params.stream().map(t -> t.ref).collect(Collectors.toList()).toArray(LLVMTypeRef[]::new);
        return new LLType(LLVM.LLVMFunctionType(result.ref, new PointerPointer<>(params.size()).put(paramRefs), params.size(), 0), new ArrayList<>(), null, result, "fn(%s) -> %s".formatted(params.stream().map(LLType::toString).collect(Collectors.joining(", ")), result), false, false, false);
    }

    private static LLType primitive(String name, LLVMTypeRef ref, boolean signedInteger, boolean unsignedInteger, boolean floatType) {
        return new LLType(ref, new ArrayList<>(), null, null, name, signedInteger, unsignedInteger, floatType);
    }

    public static LLType none() {
        return primitive("()", LLVM.LLVMVoidType(), false, false, false);
    }

//    public static LLType bool() {
//        return primitive("bool", LLVM.LLVMIntType(1), false, true, false);
//    }

    public static LLType u8() {
        return primitive("u8", LLVM.LLVMIntType(8), false, true, false);
    }

    public static LLType i32() {
        return primitive("i32", LLVM.LLVMIntType(32), true, false, false);
    }

    public static LLType u32() {
        return primitive("u32", LLVM.LLVMIntType(32), false, true, false);
    }

    public static LLType f32() {
        return primitive("f32", LLVM.LLVMFloatType(), false, false, true);
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
