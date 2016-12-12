package javax.servlet.jsp.tagext;
import javax.servlet.jsp.JspException;
public interface DynamicAttributes {
    public void setDynamicAttribute (
        String uri, String localName, Object value )
    throws JspException;
}
