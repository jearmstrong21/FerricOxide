package mackycheese21.ferricoxide.nast.hl;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.Identifier;
import mackycheese21.ferricoxide.MapStack;
import mackycheese21.ferricoxide.nast.hl.def.*;
import mackycheese21.ferricoxide.nast.hl.type.*;
import mackycheese21.ferricoxide.nast.ll.LLType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class HLContext {

    public Identifier modPath = null;
    public HLTypeId returnType = null;
    public MapStack<String, HLLocal> localStack = null;
    public List<LLType> localList = null;
    private final Map<Identifier, HLTypedefDef> typedefs;
    private final Map<Identifier, HLStructDef> structDefs;
    //    private final Map<Identifier, HLTraitDef> traitDefs;
    public final Map<Identifier, HLFunctionDef> functionDefs;
    public final Map<Identifier, HLGlobalDef> globalDefs;
    private final Map<Identifier, LLType> compiledStructs;
    public final Map<HLTypeId, HLTypeMethods> typeMethods;
//    private final Map<Identifier, LLType> compiledTraits;

    public void initialize(Identifier modPath, HLTypeId returnType) {
        this.modPath = modPath;
        this.returnType = returnType;
        localStack = new MapStack<>();
        localList = new ArrayList<>();
    }

    public HLContext(HLModule module, Map<Identifier, LLType> compiledStructs, Map<Identifier, LLType> compiledTraits) {
        this.compiledStructs = compiledStructs;
        typedefs = new HashMap<>();
        structDefs = new HashMap<>();
        functionDefs = new HashMap<>();
        globalDefs = new HashMap<>();
        typeMethods = new HashMap<>();
        for (HLTypedefDef typedef : module.typedefs) {
            typedefs.put(typedef.name, typedef);
        }
        for (HLStructDef struct : module.structs) {
            structDefs.put(struct.name, struct);
        }
        if(module.traits.size() > 0) throw new UnsupportedOperationException();
        for (HLImplDef impl : module.impls) {
            initialize(impl.modPath, null);
            HLTypeId typeId = resolve(impl.typeId);
            if(!typeMethods.containsKey(typeId)) {
                typeMethods.put(typeId, new HLTypeMethods(typeId, new HashMap<>()));
            }
            typeMethods.get(typeId).impl(impl);
        }
        for (HLFunctionDef function : module.functions) {
            functionDefs.put(function.name, function);
        }
        for (HLGlobalDef global : module.globals) {
            globalDefs.put(global.name, global);
        }
    }

    private Identifier resolveIdentifier(Identifier modPath, Identifier name, Set<Identifier> values) {
        if (values.contains(Identifier.concat(modPath, name))) return Identifier.concat(modPath, name);
        if (values.contains(name)) return name;
        return null;
    }

    public HLStructDef resolveStructDef(HLTypeId type) {
        if (type instanceof HLIdentifierTypeId idType) {
            if (structDefs.containsKey(idType.identifier)) {
                return structDefs.get(idType.identifier);
            }
        }
        throw new AnalysisException(type.span, "no struct %s".formatted(type));
    }

    public LLType compile(HLTypeId type) {
        if (type instanceof HLFunctionTypeId function)
            return LLType.function(function.params.stream().map(this::compile).collect(Collectors.toList()), compile(function.result));
        if (type instanceof HLIdentifierTypeId id) {
            if (compiledStructs.containsKey(id.identifier)) return compiledStructs.get(id.identifier);
            throw new UnsupportedOperationException(id.identifier.toString());
        }
        if (type instanceof HLTupleTypeId tuple)
            return LLType.tuple(tuple.values.stream().map(this::compile).collect(Collectors.toList()));
        if (type instanceof HLTypeId.Primitive primitive) return primitive.type;
//        if (type instanceof HLDynTypeId dyn) {
//            if (compiledTraits.containsKey(dyn.identifier)) return compiledTraits.get(dyn.identifier);
//            throw new UnsupportedOperationException();
//        }
        if (type instanceof HLPointerTypeId ptr) {
            return LLType.pointer(compile(ptr.to));
        }
        throw new UnsupportedOperationException(type.getClass().toString());
    }

    // struct -> trait -> typedef order
    public @NotNull HLTypeId resolve(HLTypeId type) {
        if (type instanceof HLFunctionTypeId function) {
            return new HLFunctionTypeId(function.span, function.params.stream().map(this::resolve).collect(Collectors.toList()), resolve(function.result));
        }
        if (type instanceof HLIdentifierTypeId id) {
            Identifier identifier = id.identifier;

            Identifier struct = resolveIdentifier(modPath, identifier, structDefs.keySet());
            if (struct != null) return new HLIdentifierTypeId(struct.span, struct);

            Identifier typedef = resolveIdentifier(modPath, identifier, typedefs.keySet());
            if (typedef != null) return resolve(new HLIdentifierTypeId(typedef.span, typedef));

            throw new AnalysisException(identifier.span, "no type %s".formatted(identifier));
        }
//        if (type instanceof HLDynTypeId dyn) {
//            Identifier trait = resolveIdentifier(modPath, dyn.identifier, traitDefs.keySet());
//            if (trait != null) return new HLIdentifierTypeId(trait.span, trait);
//
//            throw new AnalysisException(dyn.identifier.span, "no trait %s".formatted(dyn.identifier));
//        }
        if (type instanceof HLPointerTypeId pointer) {
            return new HLPointerTypeId(pointer.span, resolve(pointer.to));
        }
        if (type instanceof HLTupleTypeId tuple) {
            return new HLTupleTypeId(tuple.span, tuple.values.stream().map(this::resolve).collect(Collectors.toList()));
        }
        if (type instanceof HLTypeId.Primitive) return type;
        throw new UnsupportedOperationException(type.toString());
    }

}
