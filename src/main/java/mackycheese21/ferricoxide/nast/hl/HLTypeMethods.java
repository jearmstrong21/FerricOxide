package mackycheese21.ferricoxide.nast.hl;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.nast.hl.def.HLImplDef;
import mackycheese21.ferricoxide.nast.hl.def.HLImplFunctionDef;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;

import java.util.Map;

public class HLTypeMethods {

    public final HLTypeId type;
    public final Map<String, HLImplFunctionDef> functions;

    public HLTypeMethods(HLTypeId type, Map<String, HLImplFunctionDef> functions) {
        this.type = type;
        this.functions = functions;
    }

    public void impl(HLImplDef impl) {
        for (HLImplFunctionDef def : impl.functions) {
            if (functions.containsKey(def.proto().name.toString())) {
                throw new AnalysisException(def.proto().name.span, "duplicate impl on type " + type);
            }
            functions.put(def.proto().name.toString(), def);
        }
    }
}
