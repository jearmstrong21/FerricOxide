package mackycheese21.ferricoxide;

import mackycheese21.ferricoxide.nast.hl.HLContext;
import mackycheese21.ferricoxide.nast.hl.HLValue;
import mackycheese21.ferricoxide.nast.hl.expr.HLExpression;
import mackycheese21.ferricoxide.nast.hl.type.HLPointerTypeId;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;
import mackycheese21.ferricoxide.nast.hl.type.HLTypePredicate;
import mackycheese21.ferricoxide.nast.ll.LLType;
import mackycheese21.ferricoxide.nast.ll.LLValue;
import mackycheese21.ferricoxide.nast.ll.expr.LLAccessLocal;
import mackycheese21.ferricoxide.nast.ll.expr.LLUnary;
import mackycheese21.ferricoxide.parser.token.PunctToken;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.global.LLVM;

public enum UnaryOperator {

    DEREF(PunctToken.Type.ASTERISK),
    NEGATE(PunctToken.Type.MINUS),
    NOT(PunctToken.Type.NOT);

    public final PunctToken.Type punctuation;

    UnaryOperator(PunctToken.Type punctuation) {
        this.punctuation = punctuation;
    }

    public HLValue run(HLContext ctx, HLExpression value) {
        return switch (this) {
            case DEREF -> {
                HLTypeId derefType = value.require(ctx, HLTypePredicate.POINTER).to;
                yield new HLValue(derefType, new LLUnary(DEREF, value.value.ll()));
            }
            case NEGATE -> {
                value.require(ctx, HLTypePredicate.INT_OR_FLOAT);
                yield new HLValue(value.value.type(), new LLUnary(this, value.value.ll()));
            }
            case NOT -> {
                value.require(ctx, HLTypePredicate.INT);
                yield new HLValue(value.value.type(), new LLUnary(this, value.value.ll()));
            }
        };
    }

    public LLValue run(LLVMBuilderRef builder, LLValue value) {
        return switch (this) {
            case DEREF -> new LLValue(value.type().pointerDeref, LLVM.LLVMBuildLoad2(builder, value.type().pointerDeref.ref, value.ref(), "load"));
            case NEGATE -> new LLValue(value.type(), value.type().flags.contains(LLType.Flag.FLOAT) ? LLVM.LLVMBuildFNeg(builder, value.ref(), "neg") : LLVM.LLVMBuildNeg(builder, value.ref(), "neg"));
            case NOT -> new LLValue(value.type(), LLVM.LLVMBuildNot(builder, value.ref(), "not"));
        };
    }

}