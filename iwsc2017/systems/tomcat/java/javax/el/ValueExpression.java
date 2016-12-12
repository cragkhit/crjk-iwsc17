package javax.el;
public abstract class ValueExpression extends Expression {
    private static final long serialVersionUID = 8577809572381654673L;
    public abstract Object getValue ( ELContext context );
    public abstract void setValue ( ELContext context, Object value );
    public abstract boolean isReadOnly ( ELContext context );
    public abstract Class<?> getType ( ELContext context );
    public abstract Class<?> getExpectedType();
    public ValueReference getValueReference ( ELContext context ) {
        context.notifyBeforeEvaluation ( getExpressionString() );
        context.notifyAfterEvaluation ( getExpressionString() );
        return null;
    }
}
