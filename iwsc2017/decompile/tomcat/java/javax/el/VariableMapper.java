package javax.el;
public abstract class VariableMapper {
    public abstract ValueExpression resolveVariable ( final String p0 );
    public abstract ValueExpression setVariable ( final String p0, final ValueExpression p1 );
}
