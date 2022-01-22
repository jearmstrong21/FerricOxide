package mackycheese21.ferricoxide.ast.hl.compile;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.Pair;
import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.ast.hl.mod.FunctionDef;
import mackycheese21.ferricoxide.ast.hl.mod.GlobalDef;
import mackycheese21.ferricoxide.ast.hl.mod.StructDef;
import mackycheese21.ferricoxide.ast.hl.type.*;
import mackycheese21.ferricoxide.ast.ll.mod.LLModule;
import mackycheese21.ferricoxide.ast.ll.type.LLFunctionType;
import mackycheese21.ferricoxide.ast.ll.type.LLPointerType;
import mackycheese21.ferricoxide.ast.ll.type.LLStructType;
import mackycheese21.ferricoxide.ast.ll.type.LLType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class HLTypeLookup {

    // TODO replace Identifier with a TypeId class for generics?
    // TODO figure out how to do tuples
    // TODO reimplement traits
    public final Map<Identifier, GlobalDef> hlGlobals;
    public final Map<Identifier, FunctionDef> hlFunctions;

    private final LLModule llModule;
    private final Map<Identifier, StructDef> structDefs;
    public final Map<Identifier, LLStructType> structCompiled = new HashMap<>();

    public HLTypeLookup(Map<Identifier, GlobalDef> hlGlobals, Map<Identifier, FunctionDef> hlFunctions, LLModule llModule, Map<Identifier, StructDef> structDefs) {
        this.hlGlobals = hlGlobals;
        this.hlFunctions = hlFunctions;
        this.llModule = llModule;
        this.structDefs = structDefs;
    }

    public LLFunctionType compileFunction(Identifier modPath, HLFunctionType function) {
        return new LLFunctionType(function.params.stream().map(t -> compile(modPath, t)).collect(Collectors.toList()), compile(modPath, function.result));
    }

    public LLStructType compileTuple(Identifier modPath, HLTupleType tuple) {
        throw new UnsupportedOperationException();
    }

    public LLType compile(Identifier modPath, HLType type) {
        if (type instanceof HLFunctionType function) return compileFunction(modPath, function);
        if (type instanceof HLStructType struct)
            return Objects.requireNonNull(structCompiled.get(resolveStructDef(modPath, struct.identifier)));
        if (type instanceof HLTupleType tuple) return compileTuple(modPath, tuple);
        if (type instanceof HLKeywordType keyword) return keyword.type.compile;
        if (type instanceof HLPointerType pointer) return new LLPointerType(compile(modPath, pointer.to));
        if (type instanceof HLTraitType) throw new UnsupportedOperationException();
        throw new UnsupportedOperationException();
    }

    public Identifier resolveStructDef(Identifier modPath, Identifier struct) {
        if (structDefs.containsKey(Identifier.concat(modPath, struct)))
            return Identifier.concat(modPath, struct);
        if (structDefs.containsKey(struct)) return struct;
        throw new AnalysisException(struct.span, "no struct %s".formatted(struct));
    }

    public LLStructType lookup(Identifier identifier) {
        if (llModule.structs.containsKey(identifier.toLLVMString()))
            return llModule.structs.get(identifier.toLLVMString());
        throw new AnalysisException(identifier.span, "no struct %s".formatted(identifier));
    }

    public StructDef lookupStructDef(Identifier globalIdentifier) {
        if (structDefs.containsKey(globalIdentifier)) return structDefs.get(globalIdentifier);
        throw new UnsupportedOperationException();
    }

}
