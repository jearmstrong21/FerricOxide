package mackycheese21.ferricoxide.nast.hl;

import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;
import mackycheese21.ferricoxide.nast.ll.expr.LLExpression;

public record HLValue(HLTypeId type, LLExpression ll) {
}
