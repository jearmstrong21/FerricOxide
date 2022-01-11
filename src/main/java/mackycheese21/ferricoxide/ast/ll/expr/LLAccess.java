package mackycheese21.ferricoxide.ast.ll.expr;

public class LLAccess {

    public static class Local extends LLExpression {

        public final int index;

        public Local(int index) {
            this.index = index;
        }

        @Override
        public <T> T visit(LLExpressionVisitor<T> visitor) {
            return visitor.visitAccessLocal(this);
        }
    }

    public static class Global extends LLExpression {

        public final String name;

        public Global(String name) {
            this.name = name;
        }

        @Override
        public <T> T visit(LLExpressionVisitor<T> visitor) {
            return visitor.visitAccessGlobal(this);
        }
    }

    public static class Function extends LLExpression {
        public final String name;

        public Function(String name) {
            this.name = name;
        }

        @Override
        public <T> T visit(LLExpressionVisitor<T> visitor) {
            return visitor.visitAccessFunction(this);
        }
    }

    public static class Index extends LLExpression {

        public final LLExpression array;
        public final LLExpression index;

        public Index(LLExpression array, LLExpression index) {
            this.array = array;
            this.index = index;
        }

        @Override
        public <T> T visit(LLExpressionVisitor<T> visitor) {
            return visitor.visitAccessIndex(this);
        }
    }

    public static class Property extends LLExpression {

        public final LLExpression object;
        public final int index;

        public Property(LLExpression object, int index) {
            this.object = object;
            this.index = index;
        }

        @Override
        public <T> T visit(LLExpressionVisitor<T> visitor) {
            return visitor.visitAccessProperty(this);
        }
    }

}
