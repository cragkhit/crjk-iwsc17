// 
// Decompiled by Procyon v0.5.29
// 

package examples;

import java.io.IOException;
import javax.servlet.jsp.JspTagException;
import java.io.Writer;
import javax.servlet.jsp.JspException;

public class LogTag extends ExampleTagBase
{
    private static final long serialVersionUID = 1L;
    boolean toBrowser;
    
    public LogTag() {
        this.toBrowser = false;
    }
    
    public void setToBrowser(final String value) {
        if (value == null) {
            this.toBrowser = false;
        }
        else if (value.equalsIgnoreCase("true")) {
            this.toBrowser = true;
        }
        else {
            this.toBrowser = false;
        }
    }
    
    @Override
    public int doStartTag() throws JspException {
        return 2;
    }
    
    @Override
    public int doAfterBody() throws JspException {
        try {
            final String s = this.bodyOut.getString();
            System.err.println(s);
            if (this.toBrowser) {
                this.bodyOut.writeOut((Writer)this.bodyOut.getEnclosingWriter());
            }
            return 0;
        }
        catch (IOException ex) {
            throw new JspTagException(ex.toString());
        }
    }
}
