package javax.servlet.jsp;
import java.io.Writer;
import javax.servlet.jsp.el.VariableResolver;
import javax.el.ELContext;
import javax.servlet.jsp.el.ExpressionEvaluator;
import java.util.Enumeration;
public abstract class JspContext {
    public abstract void setAttribute ( final String p0, final Object p1 );
    public abstract void setAttribute ( final String p0, final Object p1, final int p2 );
    public abstract Object getAttribute ( final String p0 );
    public abstract Object getAttribute ( final String p0, final int p1 );
    public abstract Object findAttribute ( final String p0 );
    public abstract void removeAttribute ( final String p0 );
    public abstract void removeAttribute ( final String p0, final int p1 );
    public abstract int getAttributesScope ( final String p0 );
    public abstract Enumeration<String> getAttributeNamesInScope ( final int p0 );
    public abstract JspWriter getOut();
    public abstract ExpressionEvaluator getExpressionEvaluator();
    public abstract ELContext getELContext();
    public abstract VariableResolver getVariableResolver();
    public JspWriter pushBody ( final Writer writer ) {
        return null;
    }
    public JspWriter popBody() {
        return null;
    }
}
