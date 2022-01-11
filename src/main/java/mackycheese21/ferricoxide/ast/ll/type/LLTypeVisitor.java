package mackycheese21.ferricoxide.ast.ll.type;

public interface LLTypeVisitor<T> {

    T visitFunction(LLFunctionType type);

    T visitPointer(LLPointerType type);

    T visitPrimitive(LLPrimitiveType type);

    T visitStruct(LLStructType type);

}
