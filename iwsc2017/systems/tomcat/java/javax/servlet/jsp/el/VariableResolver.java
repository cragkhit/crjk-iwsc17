package javax.servlet.jsp.el;
@SuppressWarnings ( "dep-ann" )
public interface VariableResolver {
    public Object resolveVariable ( String pName ) throws ELException;
}
