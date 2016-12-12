package javax.servlet.jsp.el;
@SuppressWarnings ( "dep-ann" )
public abstract class Expression {
    public abstract Object evaluate ( VariableResolver vResolver )
    throws ELException;
}
