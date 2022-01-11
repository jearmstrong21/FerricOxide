package mackycheese21.ferricoxide.parser.token;

import mackycheese21.ferricoxide.ast.hl.expr.HLStringConstant;

public sealed abstract class LiteralToken extends TokenTree {

    protected LiteralToken(Span span, java.lang.String shortName) {
        super(span, shortName);
    }

    public static final class Decimal extends LiteralToken {

        public final double value;

        public Decimal(Span span, double value) {
            super(span, "decimal literal");
            this.value = value;
        }

        @Override
        public java.lang.String toString() {
            return "" + value;
        }
    }

    public static final class Integer extends LiteralToken {
        public final long value;

        public Integer(Span span, long value) {
            super(span, "integer literal");
            this.value = value;
        }

        @Override
        public java.lang.String toString() {
            return "" + value;
        }
    }

    public static final class String extends LiteralToken {
        public final java.lang.String value;

        public String(Span span, java.lang.String value) {
            super(span, "string literal");
            this.value = value;
        }

        @Override
        public java.lang.String toString() {
            return "\"%s\"".formatted(HLStringConstant.escape(value));
        }

        @Override
        public String requireStringLiteral() {
            return this;
        }
    }

    public static final class Boolean extends LiteralToken {
        public final boolean value;

        public Boolean(Span span, boolean value) {
            super(span, "boolean literal");
            this.value = value;
        }

        @Override
        public java.lang.String toString() {
            return "" + value;
        }
    }

}
