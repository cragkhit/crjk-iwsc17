// 
// Decompiled by Procyon v0.5.29
// 

package examples;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.InputStream;
import java.io.IOException;
import javax.servlet.jsp.JspTagException;
import java.util.Locale;
import javax.servlet.jsp.tagext.TagSupport;

public class ShowSource extends TagSupport
{
    private static final long serialVersionUID = 1L;
    String jspFile;
    
    public void setJspFile(final String jspFile) {
        this.jspFile = jspFile;
    }
    
    public int doEndTag() throws JspException {
        if (this.jspFile.indexOf("..") >= 0 || this.jspFile.toUpperCase(Locale.ENGLISH).indexOf("/WEB-INF/") != 0 || this.jspFile.toUpperCase(Locale.ENGLISH).indexOf("/META-INF/") != 0) {
            throw new JspTagException("Invalid JSP file " + this.jspFile);
        }
        try (final InputStream in = this.pageContext.getServletContext().getResourceAsStream(this.jspFile)) {
            if (in == null) {
                throw new JspTagException("Unable to find JSP file: " + this.jspFile);
            }
            final JspWriter out = this.pageContext.getOut();
            try {
                out.println("<body>");
                out.println("<pre>");
                for (int ch = in.read(); ch != -1; ch = in.read()) {
                    if (ch == 60) {
                        out.print("&lt;");
                    }
                    else {
                        out.print((char)ch);
                    }
                }
                out.println("</pre>");
                out.println("</body>");
            }
            catch (IOException ex) {
                throw new JspTagException("IOException: " + ex.toString());
            }
        }
        catch (IOException ex2) {
            throw new JspTagException("IOException: " + ex2.toString());
        }
        return super.doEndTag();
    }
}
