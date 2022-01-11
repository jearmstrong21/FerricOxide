import mackycheese21.ferricoxide.FOLLVM;
import mackycheese21.ferricoxide.ast.BinaryOperator;
import mackycheese21.ferricoxide.ast.ll.compile.LLModuleCompiler;
import mackycheese21.ferricoxide.ast.ll.compile.LLModuleValidator;
import mackycheese21.ferricoxide.ast.ll.compile.LLModuleWriter;
import mackycheese21.ferricoxide.ast.ll.expr.*;
import mackycheese21.ferricoxide.ast.ll.mod.LLFunction;
import mackycheese21.ferricoxide.ast.ll.mod.LLModule;
import mackycheese21.ferricoxide.ast.ll.stmt.LLAssign;
import mackycheese21.ferricoxide.ast.ll.stmt.LLCallStmt;
import mackycheese21.ferricoxide.ast.ll.stmt.LLReturn;
import mackycheese21.ferricoxide.ast.ll.type.LLFunctionType;
import mackycheese21.ferricoxide.ast.ll.type.LLPrimitiveType;
import mackycheese21.ferricoxide.ast.ll.type.LLStructType;
import mackycheese21.ferricoxide.format.LLFormatter;
import org.bytedeco.llvm.LLVM.LLVMModuleRef;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class TestLL {

    public static void main(String[] args) throws IOException {
        FOLLVM.initialize();

        LLStructType coord = new LLStructType("coord", List.of(LLPrimitiveType.I32, LLPrimitiveType.I32));
        LLModule module = new LLModule(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
        LLFunctionType printInt_type = new LLFunctionType(List.of(LLPrimitiveType.I32), LLPrimitiveType.VOID);
        LLFunctionType main_type = new LLFunctionType(List.of(), LLPrimitiveType.VOID);

        module.structs.put("coord", coord);

        module.globalTypes.put("origin", coord);
        module.globalValues.put("origin", new LLStructInit(coord, List.of(new LLIntConstant(5, LLPrimitiveType.I32), new LLBinary(new LLIntConstant(3, LLPrimitiveType.I32), BinaryOperator.ADD, new LLIntConstant(2, LLPrimitiveType.I32)))));

        module.functionTypes.put("printInt", printInt_type);
        module.functionValues.put("printInt", new LLFunction("printInt", false, printInt_type, null, null));

        module.functionTypes.put("fo_run", main_type);
        module.functionValues.put("fo_run", new LLFunction("fo_run", false, main_type, List.of(
                new LLAssign(new LLAccess.Local(0), new LLDeref(new LLAccess.Global("origin"))),
                new LLCallStmt(new LLCallExpr(new LLAccess.Function("printInt"), List.of(new LLDeref(new LLAccess.Property(new LLAccess.Local(0), 0))))),
                new LLReturn(null)
        ), List.of(
                coord
        )));

        System.out.println(LLFormatter.formatModule("\t", module));
        LLModuleValidator.validate(module);
        LLVMModuleRef moduleRef = LLModuleCompiler.compile(module);
        LLModuleWriter.write(module, moduleRef);
    }

}
