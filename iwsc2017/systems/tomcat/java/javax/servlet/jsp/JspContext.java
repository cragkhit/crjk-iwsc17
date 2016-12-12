package javax.servlet.jsp;
import java.util.Enumeration;
import javax.el.ELContext;
public abstract class JspContext {
    public JspContext() {
    }
    public abstract void setAttribute ( String name, Object value );
    public abstract void setAttribute ( String name, Object value, int scope );
    public abstract Object getAttribute ( String name );
    public abstract Object getAttribute ( String name, int scope );
    public abstract Object findAttribute ( String name );
    public abstract void removeAttribute ( String name );
    public abstract void removeAttribute ( String name, int scope );
    public abstract int getAttributesScope ( String name );
    public abstract Enumeration<String> getAttributeNamesInScope ( int scope );
    public abstract JspWriter getOut();
    @SuppressWarnings ( "dep-ann" )
    public abstract javax.servlet.jsp.el.ExpressionEvaluator getExpressionEvaluator();
    public abstract ELContext getELContext();
    @SuppressWarnings ( "dep-ann" )
    public abstract javax.servlet.jsp.el.VariableResolver getVariableResolver();
    public JspWriter pushBody ( java.io.Writer writer ) {
        return null;
    }
    public JspWriter popBody() {
        return null;
    }
}
