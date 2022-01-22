import mackycheese21.ferricoxide.FOLLVM;
import mackycheese21.ferricoxide.nast.ll.LLExpression;
import mackycheese21.ferricoxide.nast.ll.LLFunction;
import mackycheese21.ferricoxide.nast.ll.LLModule;
import mackycheese21.ferricoxide.nast.ll.LLType;
import mackycheese21.ferricoxide.nast.ll.expr.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestNNLL {

    public static void main(String[] args) throws IOException {
        FOLLVM.initialize();

        LLModule module = new LLModule("mymod");
        module.globals.put("glob", LLType.i32());
        module.functions.put("printInt", new LLFunction("printInt", 1, List.of(LLType.i32()), LLType.none(), true, null));
        module.functions.put("ret5", new LLFunction("ret5", 0, List.of(), LLType.i32(), false, new LLReturn(new LLIntConst(5))));

        LLExpression body = new LLCall(new LLAccessFunction("printInt"), List.of(
                new LLAdd(
                        new LLCall(new LLAccessFunction("ret5"), List.of()),
                        new LLIntConst(10)
                )
        ));
        module.functions.put("run", new LLFunction("run", 0, new ArrayList<>(), LLType.none(), true, body));
        module.compile();
        module.write("BIN/build/main.x86");
    }


}
