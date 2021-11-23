package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.ConcreteType;
import mackycheese21.ferricoxide.GlobalContext;
import mackycheese21.ferricoxide.Utils;
import mackycheese21.ferricoxide.Variables;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import static org.bytedeco.llvm.global.LLVM.*;

public class ArithBinary extends Ast {

    public enum Op {
        ADD(true),
        SUB(true),
        MUL(true),
        DIV(true),
        EQ(false),
        LE(false),
        LT(false),
        GE(false),
        GT(false);

        private final boolean arith;

        Op(boolean arith) {
            this.arith = arith;
        }

        public boolean isArith() {
            return arith;
        }

        public Ast on(Ast a, Ast b) {
            return new ArithBinary(this, a, b);
        }
    }

    private final Op op;
    private final Ast a;
    private final Ast b;

    private ArithBinary(Op op, Ast a, Ast b) {
        this.op = op;
        this.a = a;
        this.b = b;
    }

    @Override
    public ConcreteType getConcreteType(GlobalContext globalContext, Variables variables) {
        ConcreteType ta = a.getConcreteType(globalContext, variables);
        ConcreteType tb = b.getConcreteType(globalContext, variables);
        Utils.assertEquals(ta, tb);
        if (op.isArith()) {
            return ta;
        } else {
            return ConcreteType.BOOL;
        }
    }

    @Override
    public LLVMValueRef generateIR(GlobalContext globalContext, Variables variables, LLVMBuilderRef builder) {
        LLVMValueRef va = a.generateIR(globalContext, variables, builder);
        LLVMValueRef vb = b.generateIR(globalContext, variables, builder);
        boolean integer = a.getConcreteType(globalContext, variables).isInteger();
        getConcreteType(globalContext, variables);
        switch (op) {
            case ADD:
                if (integer) {
                    return LLVMBuildAdd(builder, va, vb, "add");
                } else {
                    return LLVMBuildFAdd(builder, va, vb, "fadd");
                }
            case SUB:
                if (integer) {
                    return LLVMBuildSub(builder, va, vb, "sub");
                } else {
                    return LLVMBuildFSub(builder, va, vb, "fsub");
                }
            case MUL:
                if (integer) {
                    return LLVMBuildMul(builder, va, vb, "mul");
                } else {
                    return LLVMBuildFMul(builder, va, vb, "fmul");
                }
            case DIV:
                if (integer) {
                    return LLVMBuildSDiv(builder, va, vb, "sdiv");
                } else {
                    return LLVMBuildFDiv(builder, va, vb, "fdiv");
                }
            case EQ:
                if (integer) {
                    return LLVMBuildICmp(builder, LLVMIntEQ, va, vb, "eq");
                } else {
                    return LLVMBuildFCmp(builder, LLVMRealOEQ, va, vb, "ordered_eq");
                }
            case LE:
                if (integer) {
                    return LLVMBuildICmp(builder, LLVMIntSLE, va, vb, "signed_le");
                } else {
                    return LLVMBuildFCmp(builder, LLVMRealOLE, va, vb, "ordered_le");
                }
            case LT:
                if (integer) {
                    return LLVMBuildICmp(builder, LLVMIntSLT, va, vb, "signed_lt");
                } else {
                    return LLVMBuildFCmp(builder, LLVMRealOLT, va, vb, "ordered_lt");
                }
            case GE:
                if (integer) {
                    return LLVMBuildICmp(builder, LLVMIntSGE, va, vb, "signed_ge");
                } else {
                    return LLVMBuildFCmp(builder, LLVMRealOGE, va, vb, "ordered_ge");
                }
            case GT:
                if (integer) {
                    return LLVMBuildICmp(builder, LLVMIntSGT, va, vb, "signed_gt");
                } else {
                    return LLVMBuildFCmp(builder, LLVMRealOGT, va, vb, "ordered_gt");
                }
        }
        throw new RuntimeException("unexpected " + op);
    }

    @Override
    public String toString() {
        return String.format("(%s) %s (%s)", a, op, b);
//        return String.format("%s[%s, %s]", op, a, b);
    }
}
