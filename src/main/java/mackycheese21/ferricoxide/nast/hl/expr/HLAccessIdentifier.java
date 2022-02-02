package mackycheese21.ferricoxide.nast.hl.expr;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.Identifier;
import mackycheese21.ferricoxide.nast.hl.HLContext;
import mackycheese21.ferricoxide.nast.hl.HLLocal;
import mackycheese21.ferricoxide.nast.hl.HLValue;
import mackycheese21.ferricoxide.nast.hl.type.HLPointerTypeId;
import mackycheese21.ferricoxide.nast.ll.expr.LLAccessFunction;
import mackycheese21.ferricoxide.nast.ll.expr.LLAccessGlobal;
import mackycheese21.ferricoxide.nast.ll.expr.LLAccessLocal;
import mackycheese21.ferricoxide.parser.token.Span;

public class HLAccessIdentifier extends HLExpression {

    public final Identifier identifier;

    public HLAccessIdentifier(Span span, Identifier identifier) {
        super(span);
        this.identifier = identifier;
    }

    @Override
    public void compile(HLContext ctx) {
        if (identifier.length() == 1) {
            String name = identifier.toString();
            if (ctx.localStack.containsKey(name)) {
                HLLocal local = ctx.localStack.get(name);
                value = new HLValue(new HLPointerTypeId(span, local.type()), new LLAccessLocal(local.index()));
                return;
            }
        }
        Identifier local = Identifier.concat(ctx.modPath, identifier);
        if (ctx.functionDefs.containsKey(local)) {
            value = new HLValue(ctx.functionDefs.get(local).typeId(), new LLAccessFunction(ctx.functionDefs.get(local).llvmName));
            return;
        }
        if (ctx.functionDefs.containsKey(identifier)) {
            value = new HLValue(ctx.functionDefs.get(identifier).typeId(), new LLAccessFunction(ctx.functionDefs.get(identifier).llvmName));
            return;
        }

        if (ctx.globalDefs.containsKey(local)) {
            value = new HLValue(new HLPointerTypeId(span, ctx.globalDefs.get(local).type), new LLAccessGlobal(local.toLLVMString()));
            return;
        }
        if (ctx.globalDefs.containsKey(identifier)) {
            value = new HLValue(new HLPointerTypeId(span, ctx.globalDefs.get(identifier).type), new LLAccessGlobal(identifier.toLLVMString()));
            return;
        }

        throw new AnalysisException(span, "no such identifier " + identifier);
    }

    @Override
    public String toString() {
        return identifier.toLLVMString();
    }

    @Override
    public boolean hasForcedReturn() {
        return false;
    }

    @Override
    public boolean hasForcedBreak() {
        return false;
    }
}
