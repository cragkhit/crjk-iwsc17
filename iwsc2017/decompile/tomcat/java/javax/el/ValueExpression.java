package javax.el;
public abstract class ValueExpression extends Expression {
    private static final long serialVersionUID = 8577809572381654673L;
    public abstract Object getValue ( final ELContext p0 );
    public abstract void setValue ( final ELContext p0, final Object p1 );
    public abstract boolean isReadOnly ( final ELContext p0 );
    public abstract Class<?> getType ( final ELContext p0 );
    public abstract Class<?> getExpectedType();
    public ValueReference getValueReference ( final ELContext context ) {
        context.notifyBeforeEvaluation ( this.getExpressionString() );
        context.notifyAfterEvaluation ( this.getExpressionString() );
        return null;
    }
}
