import mackycheese21.ferricoxide.FOLLVM;
import mackycheese21.ferricoxide.Identifier;
import mackycheese21.ferricoxide.MapStack;
import mackycheese21.ferricoxide.nast.hl.HLContext;
import mackycheese21.ferricoxide.nast.hl.HLModule;
import mackycheese21.ferricoxide.nast.hl.expr.*;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;
import mackycheese21.ferricoxide.parser.token.Span;

import java.util.List;

public class TestNNHLLL {

    public static void main(String[] args) {
        FOLLVM.initialize();
        HLExpression hl =
                new HLDiscard(Span.NONE,
                        new HLReturn(Span.NONE,
                                new HLAdd(Span.NONE,
                                        new HLIntConstant(Span.NONE, 5),
                                        new HLIntConstant(Span.NONE, 10)
                                )
                        )
                );
        HLContext ctx = new HLContext(new Identifier(Span.NONE, List.of()), HLTypeId.i32(Span.NONE), new MapStack<>(), 0, new HLModule(), compiledStructs);
        hl.compile(ctx);
        System.out.println(hl);
        System.out.println(hl.value.type());
        System.out.println(hl.value.ll());
    }

}
