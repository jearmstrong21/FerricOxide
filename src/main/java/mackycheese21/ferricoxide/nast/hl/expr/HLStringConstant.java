package mackycheese21.ferricoxide.nast.hl.expr;

import mackycheese21.ferricoxide.nast.hl.HLContext;
import mackycheese21.ferricoxide.nast.hl.HLValue;
import mackycheese21.ferricoxide.nast.hl.type.HLPointerTypeId;
import mackycheese21.ferricoxide.nast.hl.type.HLTypeId;
import mackycheese21.ferricoxide.nast.ll.expr.LLStringConstant;
import mackycheese21.ferricoxide.parser.token.Span;

public class HLStringConstant extends HLExpression {

    public final String constant;

    public HLStringConstant(Span span, String constant) {
        super(span);
        this.constant = constant;
    }

    @Override
    public void compile(HLContext ctx) {
        value = new HLValue(new HLPointerTypeId(span, HLTypeId.u8(span)), new LLStringConstant(constant));
    }

    @Override
    public String toString() {
        return "\"%s\"".formatted(escape(constant));
    }

    @Override
    public boolean hasForcedReturn() {
        return false;
    }

    @Override
    public boolean hasForcedBreak() {
        return false;
    }

    public static String escape(String value) {
        return value
                .replace("\"", "\\\"")
                .replace("'", "\\'")
                .replace("\0", "\\0")
                .replace("\n", "\\n")
                .replace("\t", "\\t")
                .replace("\\", "\\\\");
    }

    public static String unescape(String value) {
        return value
                .replace("\\\"", "\"")
                .replace("\\'", "'")
                .replace("\\0", "\0")
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\\\", "\\");
    }
}
