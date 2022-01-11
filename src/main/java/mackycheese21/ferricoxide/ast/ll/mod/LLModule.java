package mackycheese21.ferricoxide.ast.ll.mod;

import mackycheese21.ferricoxide.Pair;
import mackycheese21.ferricoxide.ast.ll.expr.LLExpression;
import mackycheese21.ferricoxide.ast.ll.type.LLFunctionType;
import mackycheese21.ferricoxide.ast.ll.type.LLStructType;
import mackycheese21.ferricoxide.ast.ll.type.LLType;

import java.util.Map;

public class LLModule {

    public final Map<String, LLType> globalTypes;
    public final Map<String, LLExpression> globalValues;
    public final Map<String, LLStructType> structs;
    public final Map<String, LLFunctionType> functionTypes;
    public final Map<String, LLFunction> functionValues;

    public LLModule(
            Map<String, LLType> globalTypes,
            Map<String, LLExpression> globalValues,
            Map<String, LLStructType> structs,
            Map<String, LLFunctionType> functionTypes,
            Map<String, LLFunction> functionValues) {
        this.globalTypes = globalTypes;
        this.globalValues = globalValues;
        this.structs = structs;
        this.functionTypes = functionTypes;
        this.functionValues = functionValues;
    }
}
