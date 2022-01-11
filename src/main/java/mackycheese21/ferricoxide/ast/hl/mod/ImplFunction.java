package mackycheese21.ferricoxide.ast.hl.mod;

import mackycheese21.ferricoxide.ast.hl.stmt.HLStatement;

import java.util.List;

public class ImplFunction {

    public final FunctionPrototype prototype;
    public final List<HLStatement> body;

    public ImplFunction(FunctionPrototype prototype, List<HLStatement> body) {
        this.prototype = prototype;
        this.body = body;
    }
}
