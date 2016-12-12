package javax.el;
public abstract class MethodExpression extends Expression {
    private static final long serialVersionUID = 8163925562047324656L;
    public abstract MethodInfo getMethodInfo ( ELContext context );
    public abstract Object invoke ( ELContext context, Object[] params );
    public boolean isParametersProvided() {
        return false;
    }
    @Deprecated
    public boolean isParmetersProvided() {
        return false;
    }
}
