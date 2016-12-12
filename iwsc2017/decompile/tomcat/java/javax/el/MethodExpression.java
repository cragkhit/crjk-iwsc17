package javax.el;
public abstract class MethodExpression extends Expression {
    private static final long serialVersionUID = 8163925562047324656L;
    public abstract MethodInfo getMethodInfo ( final ELContext p0 );
    public abstract Object invoke ( final ELContext p0, final Object[] p1 );
    public boolean isParametersProvided() {
        return false;
    }
    @Deprecated
    public boolean isParmetersProvided() {
        return false;
    }
}
