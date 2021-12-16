package mackycheese21.ferricoxide.ast.module;

import mackycheese21.ferricoxide.ast.Identifier;
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

// mutable data class
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
        for (GlobalVariable global : globals) {
            System.out.println("TODO GLOBAL NAMESPACE");
            statements.add(new Assign(new RefAccessVar(new Identifier[]{global.name}), global.value, BinaryOperator.DISCARD_FIRST));
        }
        statements.add(new ReturnStmt(new IntConstant(0)));
        FunctionType functionType = FunctionType.of(ConcreteType.I32, new ArrayList<>());
        Function function = new Function(new Identifier("fo_global_init", true), false, functionType, new ArrayList<>(), new Block(statements), "fo_global_init");
        functions.add(function);
    }

}
