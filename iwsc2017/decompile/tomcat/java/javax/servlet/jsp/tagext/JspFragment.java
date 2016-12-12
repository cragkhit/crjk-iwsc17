package javax.servlet.jsp.tagext;
import javax.servlet.jsp.JspContext;
import java.io.IOException;
import javax.servlet.jsp.JspException;
import java.io.Writer;
public abstract class JspFragment {
    public abstract void invoke ( final Writer p0 ) throws JspException, IOException;
    public abstract JspContext getJspContext();
}
