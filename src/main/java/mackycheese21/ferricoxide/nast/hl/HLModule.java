package mackycheese21.ferricoxide.nast.hl;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.BinaryOperator;
import mackycheese21.ferricoxide.Identifier;
import mackycheese21.ferricoxide.Pair;
import mackycheese21.ferricoxide.nast.hl.def.*;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;
import mackycheese21.ferricoxide.nast.ll.LLFunction;
import mackycheese21.ferricoxide.nast.ll.LLModule;
import mackycheese21.ferricoxide.nast.ll.LLType;
import mackycheese21.ferricoxide.nast.ll.expr.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HLModule {

    public final List<HLImplDef> impls;
    public final List<HLStructDef> structs;
    public final List<HLTraitDef> traits;
    public final List<HLGlobalDef> globals;
    public final List<HLFunctionDef> functions;
    public final List<HLTypedefDef> typedefs;

    public HLModule() {
        impls = new ArrayList<>();
        structs = new ArrayList<>();
        traits = new ArrayList<>();
        globals = new ArrayList<>();
        functions = new ArrayList<>();
        typedefs = new ArrayList<>();
    }

    public LLModule compile() {
        LLModule module = new LLModule("main");

        // TODO: initialize globals

        // Struct prototype
        Map<Identifier, LLType> compiledStructs = new HashMap<>();
        for (HLStructDef struct : structs) {
            compiledStructs.put(struct.name, LLType.emptyStruct(struct.name));
        }
        HLContext ctx = new HLContext(this, compiledStructs);

        // Struct body
        for (HLStructDef struct : structs) {
            ctx.initialize(struct.name.removeLast(), null);
            compiledStructs.get(struct.name).setFields(struct.fields.stream().map(Pair::y).map(ctx::compile).collect(Collectors.toList()));
        }

        // Global prototype
        for (HLGlobalDef global : globals) {
            ctx.initialize(global.name.removeLast(), null);
            module.globals.put(global.name.toLLVMString(), ctx.compile(global.type));
        }

        // Global initialize
        {
            List<LLExpression> globalInit = new ArrayList<>();
            ctx.initialize(null, null);
            for (HLGlobalDef global : globals) {
                ctx.modPath = global.name.removeLast();
                if (global.value.hasForcedReturn())
                    throw new AnalysisException(global.span, "cannot return from global");
                global.value.compile(ctx);
                globalInit.add(new LLBinary(BinaryOperator.ASSIGN, new LLAccessGlobal(global.name.toLLVMString()), global.value.value.ll()));
            }
            globalInit.add(new LLReturn(new LLNone()));
            LLFunction function = new LLFunction("fo_global_init", ctx.localList.size(), ctx.localList, LLType.none(), true, new LLBlock(globalInit));
            module.functions.put("fo_global_init", function);
        }

        // Function body
        for (HLFunctionDef function : functions) {
            ctx.initialize(function.name.removeLast(), function.result);
            ctx.localStack.push();
            for (int i = 0; i < function.params.size(); i++) {
                HLTypeId resolvedParam = ctx.resolve(function.params.get(i).y());
                ctx.localList.add(ctx.compile(resolvedParam));
                ctx.localStack.put(function.params.get(i).x(), new HLLocal(ctx.localList.size() - 1, resolvedParam));
            }
            LLExpression body = null;
            if (function.body != null) {
                function.body.compile(ctx);
                body = function.body.value.ll();
                if (!function.body.hasForcedReturn()) {
                    function.body.require(ctx, function.result.pred());
                    body = new LLReturn(body);
                }
            }
//            ctx.localList.addAll(0, function.params.stream().map(Pair::y).map(ctx::compile).collect(Collectors.toList()));
//            ctx.localStack.pu
            module.functions.put(function.llvmName, new LLFunction(function.llvmName, function.params.size(), ctx.localList, ctx.compile(function.result), function.export, body));
        }

        return module;
    }

}
