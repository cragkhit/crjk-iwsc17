// 
// Decompiled by Procyon v0.5.29
// 

package examples;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

public class ValuesTag extends TagSupport
{
    private static final long serialVersionUID = 1L;
    private Object objectValue;
    private String stringValue;
    private long longValue;
    private double doubleValue;
    
    public ValuesTag() {
        this.objectValue = "-1";
        this.stringValue = "-1";
        this.longValue = -1L;
        this.doubleValue = -1.0;
    }
    
    public void setObject(final Object objectValue) {
        this.objectValue = objectValue;
    }
    
    public void setString(final String stringValue) {
        this.stringValue = stringValue;
    }
    
    public void setLong(final long longValue) {
        this.longValue = longValue;
    }
    
    public void setDouble(final double doubleValue) {
        this.doubleValue = doubleValue;
    }
    
    public int doEndTag() throws JspException {
        final JspWriter out = this.pageContext.getOut();
        try {
            if (!"-1".equals(this.objectValue)) {
                out.print(this.objectValue);
            }
            else if (!"-1".equals(this.stringValue)) {
                out.print(this.stringValue);
            }
            else if (this.longValue != -1L) {
                out.print(this.longValue);
            }
            else if (this.doubleValue != -1.0) {
                out.print(this.doubleValue);
            }
            else {
                out.print("-1");
            }
        }
        catch (IOException ex) {
            throw new JspTagException("IOException: " + ex.toString(), (Throwable)ex);
        }
        return super.doEndTag();
    }
}
