package mackycheese21.ferricoxide.nast.ll;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public record LLFunction(String name,
                         int paramCount,
                         List<LLType> locals,
                         LLType result,
                         boolean externLinkage,
                         @Nullable LLExpression body) {

}
