package mackycheese21.ferricoxide.ast.hl.expr;

import mackycheese21.ferricoxide.ast.Identifier;
import mackycheese21.ferricoxide.parser.token.Span;

public final class HLAccess {

    private HLAccess() {

    }

    public static class Var extends HLExpression {

        public final Identifier identifier;
        public boolean ref;

        public Var(Span span, Identifier identifier, boolean ref) {
            super(span);
            this.identifier = identifier;
            this.ref = ref;
        }

        @Override
        public <T> T visit(HLExpressionVisitor<T> visitor) {
            return visitor.visitAccessVar(this);
        }
    }

    public static class Index extends HLExpression {

        public final HLExpression array;
        public final HLExpression index;
        public boolean ref;

        public Index(Span span, HLExpression array, HLExpression index, boolean ref) {
            super(span);
            this.array = array;
            this.index = index;
            this.ref = ref;
        }

        @Override
        public <T> T visit(HLExpressionVisitor<T> visitor) {
            return visitor.visitAccessIndex(this);
        }
    }

    public static final class Property {

        private Property() {

        }

        public static class Name extends HLExpression {
            public final HLExpression object;
            public final String name;
            public boolean ref;

            public Name(Span span, HLExpression object, String name, boolean ref) {
                super(span);
                this.object = object;
                this.name = name;
                this.ref = ref;
            }

            @Override
            public <T> T visit(HLExpressionVisitor<T> visitor) {
                return visitor.visitAccessPropertyName(this);
            }
        }

        public static class Index extends HLExpression {
            public final HLExpression object;
            public final int index;
            public boolean ref;

            public Index(Span span, HLExpression object, int index, boolean ref) {
                super(span);
                this.object = object;
                this.index = index;
                this.ref = ref;
            }

            @Override
            public <T> T visit(HLExpressionVisitor<T> visitor) {
                return visitor.visitAccessPropertyIndex(this);
            }
        }

    }

}
