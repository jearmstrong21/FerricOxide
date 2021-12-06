package mackycheese21.ferricoxide.ast.module;

import mackycheese21.ferricoxide.ast.expr.BinaryOperator;
import mackycheese21.ferricoxide.ast.expr.IntConstant;
import mackycheese21.ferricoxide.ast.expr.RefAccessVar;
import mackycheese21.ferricoxide.ast.stmt.Assign;
import mackycheese21.ferricoxide.ast.stmt.Block;
import mackycheese21.ferricoxide.ast.stmt.ReturnStmt;
import mackycheese21.ferricoxide.ast.stmt.Statement;
import mackycheese21.ferricoxide.ast.type.ConcreteType;
import mackycheese21.ferricoxide.ast.type.FunctionType;
import mackycheese21.ferricoxide.ast.type.StructType;
import mackycheese21.ferricoxide.ast.visitor.ResolveVisitor;

import java.util.ArrayList;
import java.util.List;

public class FOModule {

    public final List<GlobalVariable> globals;
    public final List<StructType> structs;
    public final List<Function> functions;

    public FOModule(List<GlobalVariable> globals, List<StructType> structs, List<Function> functions) {
        this.globals = globals;
        this.structs = structs;
        this.functions = functions;
    }

    public void resolve() {
        new ResolveVisitor().visit(this);
    }

    public void initializeGlobals() {
        List<Statement> statements = new ArrayList<>();
        for (int i = 0; i < globals.size(); i++) {
            statements.add(new Assign(new RefAccessVar(globals.get(i).name), globals.get(i).value, BinaryOperator.DISCARD_FIRST));
        }
        statements.add(new ReturnStmt(new IntConstant(0)));
        FunctionType functionType = FunctionType.of(ConcreteType.I32, new ArrayList<>());
        Function function = new Function("fo__runtime_global_init", false, functionType, new ArrayList<>(), new Block(statements));
        functions.add(function);
    }

}
