package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.ConcreteType;
import mackycheese21.ferricoxide.GlobalContext;
import mackycheese21.ferricoxide.Variables;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Block extends Ast {

    private final List<Ast> asts;

    public Block(Ast... asts) {
        this(Arrays.asList(asts));
    }

    public Block(List<Ast> asts) {
        this.asts = Collections.unmodifiableList(asts);
        if (asts.size() == 0) throw new RuntimeException();
    }

    @Override
    public ConcreteType getConcreteType(GlobalContext globalContext, Variables variables) {
        for (int i = 0; i < asts.size() - 1; i++) {
            asts.get(i).getConcreteType(globalContext, variables);
        }
        return asts.get(asts.size() - 1).getConcreteType(globalContext, variables);
    }

    @Override
    public LLVMValueRef generateIR(GlobalContext globalContext, Variables variables, LLVMBuilderRef builder) {
        for (int i = 0; i < asts.size() - 1; i++) {
            asts.get(i).generateIR(globalContext, variables, builder);
        }
        return asts.get(asts.size() - 1).generateIR(globalContext, variables, builder);
    }

    @Override
    public String toString() {
        return String.format("{ %s }", asts.stream().map(Ast::toString).collect(Collectors.joining("; ")));
    }
}
