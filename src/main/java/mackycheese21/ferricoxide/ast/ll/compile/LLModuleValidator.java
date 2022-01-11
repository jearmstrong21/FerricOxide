package mackycheese21.ferricoxide.ast.ll.compile;

import mackycheese21.ferricoxide.ast.ll.mod.LLFunction;
import mackycheese21.ferricoxide.ast.ll.mod.LLModule;

import java.util.ArrayList;

public class LLModuleValidator {

    private static void validateGlobals(LLModule module) {
        for (String name : module.globalValues.keySet()) {
            module.globalValues.get(name).visit(new LLExpressionValidator(
                    new ArrayList<>(),
                    module.globalTypes,
                    module.functionTypes));
        }
    }

    private static void validateFunctions(LLModule module) {
        for (LLFunction function : module.functionValues.values()) {
            if (function.statements != null) {
                LLStatementValidator statementValidator = new LLStatementValidator(function.type.result, function.locals, module.globalTypes, new LLExpressionValidator(function.locals, module.globalTypes, module.functionTypes));
                if (function.locals.size() < function.type.params.size()) throw new UnsupportedOperationException();
                for (int i = 0; i < function.type.params.size(); i++) {
                    if (!function.type.params.get(i).equals(function.locals.get(i))) {
                        throw new UnsupportedOperationException();
                    }
                }
                function.statements.forEach(s -> s.visit(statementValidator));
            }
        }
    }

    public static void validate(LLModule module) {
        validateGlobals(module);
        validateFunctions(module);
    }

}
